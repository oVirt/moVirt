package org.ovirt.mobile.movirt.sync;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import com.android.internal.util.Predicate;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.facade.intent.EntityIntentResolver;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.mainactivity.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EBean(scope = EBean.Scope.Singleton)
public class DbUpdater {
    private static final String TAG = DbUpdater.class.getSimpleName();

    @RootContext
    Context context;

    @Bean
    ProviderFacade provider;

    @Bean
    NotificationHelper notificationHelper;

    public <E extends OVirtEntity> DbUpdater.UpdateLocalEntitiesBuilder<E> update(Class<E> clazz) {
        return new UpdateLocalEntitiesBuilder<>(clazz);
    }

    private <E extends OVirtEntity> void updateLocalEntity(E remoteEntity, UpdateLocalEntitiesBuilder<E> args) {
        if (remoteEntity == null) {
            return;
        }

        final ProviderFacade.BatchBuilder batch = provider.batch();

        Collection<E> localEntities = provider.query(args.clazz).id(remoteEntity.getId()).all();

        if (localEntities.isEmpty()) {
            if (args.checkTriggersForNewEntities) {
                resolveTriggers(null, remoteEntity, args);
            }
            Log.d(TAG, String.format("%s: scheduling insert for id = %s", args.clazz.getSimpleName(), remoteEntity.getId()));

            batch.insert(remoteEntity);
        } else {
            E localEntity = localEntities.iterator().next();

            List<Pair<E, E>> entities = new ArrayList<>();
            entities.add(new Pair<>(localEntity, remoteEntity));
            checkEntitiesChanged(entities, batch, args);
        }

        applyBatch(batch);
        displayNotification(args);
    }

    private <E extends OVirtEntity> void updateLocalEntities(List<E> remoteEntities, UpdateLocalEntitiesBuilder<E> args) throws RemoteException {
        if (remoteEntities == null) {
            remoteEntities = Collections.emptyList();
        }

        final Map<String, E> remoteEntityMap = groupEntitiesById(remoteEntities);
        final EntityMapper<E> mapper = EntityMapper.forEntity(args.clazz);

        ProviderFacade.BatchBuilder batch = provider.batch();
        List<Pair<E, E>> entities = new ArrayList<>();

        final ProviderFacade.QueryBuilder<E> queryBuilder = provider.query(args.clazz);

        if (args.accountId != null) {
            queryBuilder.where(OVirtContract.AccountEntity.ACCOUNT_ID, args.accountId);
        }

        final Cursor cursor = queryBuilder.asCursor();
        if (cursor == null) {
            return;
        }

        try {
            while (cursor.moveToNext()) {
                E localEntity = mapper.fromCursor(cursor);
                if (args.scopePredicate == null || args.scopePredicate.apply(localEntity)) { // apply if there is no withScopePredicate
                    E remoteEntity = remoteEntityMap.get(localEntity.getId());
                    if (remoteEntity == null) { // local entity obsolete, schedule delete from db
                        if (args.removeExpiredEntities) { // except for partial updates
                            Log.d(TAG, String.format("%s: scheduling delete for URI = %s", args.clazz.getSimpleName(), localEntity.getUri()));
                            batch.delete(localEntity);
                        }
                    } else { // existing entity, update stats if changed
                        remoteEntityMap.remove(localEntity.getId());
                        if (args.updateChangedEntities) {
                            entities.add(new Pair<>(localEntity, remoteEntity));
                        }
                    }
                }
            }
            checkEntitiesChanged(entities, batch, args);
        } finally {
            ObjectUtils.closeSilently(cursor);
        }

        for (E entity : remoteEntityMap.values()) {
            Log.d(TAG, String.format("%s: scheduling insert for id = %s", args.clazz.getSimpleName(), entity.getId()));
            if (args.beforeInsertCallback != null) {
                args.beforeInsertCallback.before(entity);
            }
            if (args.checkTriggersForNewEntities) {
                resolveTriggers(null, entity, args);
            }
            batch.insert(entity);
        }

        applyBatch(batch);
        displayNotification(args);
    }

    private void applyBatch(ProviderFacade.BatchBuilder batch) {
        if (batch.isEmpty()) {
            Log.d(TAG, "No updates necessary");
        } else {
            Log.d(TAG, "Applying batch update");
            batch.apply();
        }
    }

    private <E extends OVirtEntity> void checkEntitiesChanged(List<Pair<E, E>> entities, ProviderFacade.BatchBuilder batch, UpdateLocalEntitiesBuilder<E> args) {
        for (Pair<E, E> pair : entities) {
            E localEntity = pair.first;
            E remoteEntity = pair.second;

            if (args.beforeUpdatabilityResolvedCallback != null) {
                args.beforeUpdatabilityResolvedCallback.before(localEntity, remoteEntity);
            }

            if (!localEntity.equals(remoteEntity)) {
                resolveTriggers(localEntity, remoteEntity, args);
                Log.d(TAG, String.format("%s: scheduling update for URI = %s", localEntity.getClass().getSimpleName(), localEntity.getUri()));
                batch.update(remoteEntity);
            }
        }
    }

    /**
     * @param localEntity can be null when inserting (e.g. events)
     */
    private <E extends OVirtEntity> void resolveTriggers(E localEntity, E remoteEntity,
                                                         UpdateLocalEntitiesBuilder<E> args) {
        if (args.hasTriggerResolver()) {
            final List<Trigger> triggers = args.triggerResolver.getFilteredTriggers(args.account, remoteEntity, args.getCachedTriggers(false));
            Log.d(TAG, String.format("%s: processing triggers for id = %s", remoteEntity.getClass().getSimpleName(), remoteEntity.getId()));

            for (Trigger trigger : triggers) {
                if ((localEntity == null || !trigger.getCondition().evaluate(localEntity)) && trigger.getCondition().evaluate(remoteEntity)) {
                    args.triggerResponses.add(new Pair<>(remoteEntity, trigger));
                }
            }
        }
    }

    private <E extends OVirtEntity> void displayNotification(UpdateLocalEntitiesBuilder<E> args) {

        if (args.triggerResponses.size() == 0) {
            return;
        }
        Intent resultIntent = null;

        if (args.oneTriggerAction != null && args.triggerResponses.size() == 1 && args.oneTriggerAction.hasIntent(args.triggerResponses.get(0).first)) {
            E entity = args.triggerResponses.get(0).first;
            resultIntent = args.oneTriggerAction.getDetailIntent(entity, context);
            resultIntent.setData(entity.getUri());
        } else if (args.multipleTriggerAction != null) {
            resultIntent = new Intent(context, MainActivity_.class);
            resultIntent.setAction(args.multipleTriggerAction.name());
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        notificationHelper.showTriggersNotification(
                args.account, args.triggerResponses, context, PendingIntent.getActivity(context, 0, resultIntent, 0));
    }

    private static <E extends OVirtEntity> Map<String, E> groupEntitiesById(List<E> entities) {
        Map<String, E> entityMap = new HashMap<>();
        for (E entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
        return entityMap;
    }

    public class UpdateLocalEntitiesBuilder<E extends OVirtEntity> implements Cloneable {
        private final Class<E> clazz;

        private Predicate<E> scopePredicate;
        private boolean removeExpiredEntities = true;
        private boolean updateChangedEntities = true;
        private boolean checkTriggersForNewEntities = false;

        private TriggerResolver<E> triggerResolver;
        private EntityIntentResolver<E> oneTriggerAction;
        private MainActivityFragments multipleTriggerAction;

        private String accountId;
        private MovirtAccount account;

        private BeforeUpdatabilityResolvedCallback<E> beforeUpdatabilityResolvedCallback;
        private BeforeInsertCallback<E> beforeInsertCallback;

        // used and settable only by DbUpdater
        private Collection<Trigger> cachedTriggers;
        private List<Pair<E, Trigger>> triggerResponses = new ArrayList<>();

        public UpdateLocalEntitiesBuilder(Class<E> clazz) {
            this.clazz = clazz;
        }

        public UpdateLocalEntitiesBuilder<E> doNotRemoveExpired() {
            this.removeExpiredEntities = false;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> doNotUpdateChanged() {
            this.updateChangedEntities = false;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> checkTriggersForNewEntities() {
            this.checkTriggersForNewEntities = true;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> withScopePredicate(Predicate<E> predicate) {
            this.scopePredicate = predicate;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> withTriggerResolver(TriggerResolver<E> triggerResolver, MovirtAccount account) {
            this.triggerResolver = triggerResolver;
            this.account = account;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> triggeredActions(EntityIntentResolver<E> oneTriggerAction, MainActivityFragments multipleTriggerAction) {
            this.oneTriggerAction = oneTriggerAction;
            this.multipleTriggerAction = multipleTriggerAction;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> withBeforeUpdatabilityResolvedCallback(BeforeUpdatabilityResolvedCallback<E> beforeUpdatabilityResolvedCallback) {
            this.beforeUpdatabilityResolvedCallback = beforeUpdatabilityResolvedCallback;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> withBeforeInsertCallback(BeforeInsertCallback<E> beforeInsertCallback) {
            this.beforeInsertCallback = beforeInsertCallback;
            return this;
        }

        public UpdateLocalEntitiesBuilder<E> whereAccount(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public void updateEntity(E entity) throws RemoteException {
            updateLocalEntity(entity, getCopy());
        }

        public void updateEntities(List<E> remoteEntities) throws RemoteException {
            updateLocalEntities(remoteEntities, getCopy());
        }

        public CompositeResponse<E> asUpdateEntityResponse() {
            final UpdateLocalEntitiesBuilder<E> params = getCopy();

            return new CompositeResponse<>(new SimpleResponse<E>() {
                @Override
                public void onResponse(E entity) throws RemoteException {
                    updateLocalEntity(entity, params);
                }
            });
        }

        public CompositeResponse<List<E>> asUpdateEntitiesResponse() {
            final UpdateLocalEntitiesBuilder<E> params = getCopy();

            return new CompositeResponse<>(new SimpleResponse<List<E>>() {
                @Override
                public void onResponse(List<E> entities) throws RemoteException {
                    updateLocalEntities(entities, params);
                }
            });
        }

        private boolean hasTriggerResolver() {
            return triggerResolver != null && account != null;
        }

        private Collection<Trigger> getCachedTriggers(boolean refreshCache) {
            if (cachedTriggers == null || refreshCache) {
                cachedTriggers = (hasTriggerResolver()) ? triggerResolver.getAllTriggers() : Collections.emptyList();
            }

            return cachedTriggers;
        }

        @SuppressWarnings("unchecked")
        private UpdateLocalEntitiesBuilder<E> getCopy() {
            UpdateLocalEntitiesBuilder<E> params;
            try {
                params = (UpdateLocalEntitiesBuilder<E>) clone();
                if (triggerResponses instanceof ArrayList) {
                    params.triggerResponses = (List) ((ArrayList) triggerResponses).clone();
                }
            } catch (CloneNotSupportedException ignore) {
                params = new UpdateLocalEntitiesBuilder<>(clazz);
            }
            return params;
        }
    }

    public interface BeforeUpdatabilityResolvedCallback<E extends OVirtEntity> {
        void before(E localEntity, E remoteEntity);
    }

    public interface BeforeInsertCallback<E extends OVirtEntity> {
        void before(E remoteEntity);
    }
}
