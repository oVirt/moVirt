package org.ovirt.mobile.movirt.sync;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import com.android.internal.util.Predicate;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.facade.EntityFacade;
import org.ovirt.mobile.movirt.facade.EntityFacadeLocator;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();
    public static volatile boolean inSync = false;

    @RootContext
    Context context;
    @Bean
    OVirtClient oVirtClient;
    @Bean
    ProviderFacade provider;
    @Bean
    EntityFacadeLocator entityFacadeLocator;
    @Bean
    EventsHandler eventsHandler;
    @Bean
    MovirtAuthenticator authenticator;
    @Bean
    NotificationHelper notificationHelper;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    private static <E extends OVirtEntity> Map<String, E> groupEntitiesById(List<E> entities) {
        Map<String, E> entityMap = new HashMap<>();
        for (E entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
        return entityMap;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient providerClient, SyncResult syncResult) {

        if (inSync) {
            return;
        }

        if (!authenticator.accountConfigured()) {
            Log.d(TAG, "Account not configured, not performing sync");
            return;
        }

        try {
            sendSyncIntent(true);

            updateClusters();
            updateDataCenters();
            facadeSync(Vm.class);
            facadeSync(Host.class);
            facadeSync(StorageDomain.class);
            eventsHandler.updateEvents(false);

            sendSyncIntent(false);
        } catch (Exception e) {
            Log.e(TAG, "Error updating data", e);
            Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
            intent.putExtra(Broadcasts.Extras.CONNECTION_FAILURE_REASON, e.getMessage());
            context.sendBroadcast(intent);
        }
    }

    private void updateClusters() {
        oVirtClient.getClusters(getUpdateEntitiesResponse(Cluster.class));
    }

    private void updateDataCenters() {
        oVirtClient.getDataCenters(getUpdateEntitiesResponse(DataCenter.class));
    }

    private <E extends OVirtEntity> void facadeSync(final Class<E> clazz) {
        final EntityFacade<E> entityFacade = entityFacadeLocator.getFacade(clazz);
        entityFacade.syncAll();
    }

    public <E extends OVirtEntity> OVirtClient.SimpleResponse<E> getUpdateEntityResponse(final Class<E> clazz) {
        return new OVirtClient.SimpleResponse<E>() {
            @Override
            public void onResponse(E entity) throws RemoteException {
                updateLocalEntity(entity, clazz);
            }
        };
    }

    public <E extends OVirtEntity> void updateLocalEntity(E entity, final Class<E> clazz) {
        final EntityFacade<E> entityFacade = entityFacadeLocator.getFacade(clazz);
        final ProviderFacade.BatchBuilder batch = provider.batch();

        Collection<Trigger<E>> allTriggers = entityFacade.getAllTriggers();
        updateLocalEntity(entity, clazz, allTriggers, batch);
        applyBatch(batch);
    }

    public <E extends OVirtEntity> OVirtClient.SimpleResponse<List<E>> getUpdateEntitiesResponse(final Class<E> clazz) {
        return new OVirtClient.SimpleResponse<List<E>>() {
            @Override
            public void onResponse(List<E> entities) throws RemoteException {
                updateLocalEntities(entities, clazz);
            }
        };
    }

    public <E extends OVirtEntity> OVirtClient.SimpleResponse<List<E>> getUpdateEntitiesResponse(final Class<E> clazz, final Predicate<E> scopePredicate) {
        return new OVirtClient.SimpleResponse<List<E>>() {
            @Override
            public void onResponse(List<E> entities) throws RemoteException {
                updateLocalEntities(entities, clazz, scopePredicate);
            }
        };
    }

    private void applyBatch(ProviderFacade.BatchBuilder batch) {
        if (batch.isEmpty()) {
            Log.i(TAG, "No updates necessary");
        } else {
            Log.i(TAG, "Applying batch update");
            batch.apply();
        }
    }

    public <E extends OVirtEntity> void updateLocalEntities(List<E> remoteEntities, Class<E> clazz) throws RemoteException {
        updateLocalEntities(remoteEntities, clazz, new Predicate<E>() {
            @Override
            public boolean apply(E entity) {
                return true;
            }
        });
    }

    public <E extends OVirtEntity> void updateLocalEntities(List<E> remoteEntities, Class<E> clazz, Predicate<E> scopePredicate)
            throws RemoteException {
        final Map<String, E> entityMap = groupEntitiesById(remoteEntities);
        final EntityMapper<E> mapper = EntityMapper.forEntity(clazz);
        final EntityFacade<E> entityFacade = entityFacadeLocator.getFacade(clazz);
        Collection<Trigger<E>> allTriggers = new ArrayList<>();
        if (entityFacade != null) {
            allTriggers = entityFacade.getAllTriggers();
        }

        final Cursor cursor = provider.query(clazz).asCursor();
        if (cursor == null) {
            return;
        }

        ProviderFacade.BatchBuilder batch = provider.batch();
        List<Pair<E, E>> entities = new ArrayList<>();

        while (cursor.moveToNext()) {
            E localEntity = mapper.fromCursor(cursor);
            if (scopePredicate.apply(localEntity)) {
                E remoteEntity = entityMap.get(localEntity.getId());
                if (remoteEntity == null) { // local entity obsolete, schedule delete from db
                    Log.i(TAG, "Scheduling delete for URI" + localEntity.getUri());
                    batch.delete(localEntity);
                } else { // existing entity, update stats if changed
                    entityMap.remove(localEntity.getId());
                    entities.add(new Pair<>(localEntity, remoteEntity));
                }
            }
        }

        checkEntitiesChanged(entities, entityFacade, allTriggers, batch);
        cursor.close();

        for (E entity : entityMap.values()) {
            Log.i(TAG, "Scheduling insert for entity: id = " + entity.getId());
            batch.insert(entity);
        }

        applyBatch(batch);
    }

    private <E extends OVirtEntity> void updateLocalEntity(E remoteEntity, Class<E> clazz, Collection<Trigger<E>> allTriggers, ProviderFacade.BatchBuilder batch) {
        final EntityFacade<E> triggerResolver = entityFacadeLocator.getFacade(clazz);

        Collection<E> localEntities = provider.query(clazz).id(remoteEntity.getId()).all();
        if (localEntities.isEmpty()) {
            Log.i(TAG, "Scheduling insert for entity: id = " + remoteEntity.getId());
            batch.insert(remoteEntity);
        } else {
            E localEntity = localEntities.iterator().next();
            checkEntityChanged(localEntity, remoteEntity, triggerResolver, allTriggers, batch);
        }
    }

    private <E extends OVirtEntity> void checkEntityChanged(E localEntity, E remoteEntity, EntityFacade<E> entityFacade, Collection<Trigger<E>> allTriggers, ProviderFacade.BatchBuilder batch) {
        List<Pair<E, E>> entities = new ArrayList<>();
        entities.add(new Pair<>(localEntity, remoteEntity));
        checkEntitiesChanged(entities, entityFacade, allTriggers, batch);
    }

    private <E extends OVirtEntity> void checkEntitiesChanged(List<Pair<E, E>> entities, EntityFacade<E> entityFacade, Collection<Trigger<E>> allTriggers, ProviderFacade.BatchBuilder batch) {

        List<Pair<E, Trigger<E>>> entitiesAndTriggers = new ArrayList<>();
        for (Pair<E, E> pair : entities) {
            E localEntity = pair.first;
            E remoteEntity = pair.second;

            if (!localEntity.equals(remoteEntity)) {
                if (entityFacade != null) {
                    final List<Trigger<E>> triggers = entityFacade.getTriggers(localEntity, allTriggers);
                    Log.i(TAG, "Processing triggers for entity: " + remoteEntity.getId());

                    for (Trigger<E> trigger : triggers) {
                        if (!trigger.getCondition().evaluate(localEntity) && trigger.getCondition().evaluate(remoteEntity)) {
                            entitiesAndTriggers.add(new Pair<>(remoteEntity, trigger));
                        }
                    }
                }
                Log.i(TAG, "Scheduling update for URI: " + localEntity.getUri());
                batch.update(remoteEntity);
            }
        }
        displayNotification(entitiesAndTriggers, entityFacade);
    }

    private <E extends OVirtEntity> void displayNotification(List<Pair<E, Trigger<E>>> entitiesAndTriggers, EntityFacade<E> entityFacade) {
        if (entitiesAndTriggers.size() == 0) {
            return;
        }
        Intent resultIntent;

        if (entitiesAndTriggers.size() == 1) {
            E entity = entitiesAndTriggers.get(0).first;
            final Context appContext = getContext().getApplicationContext();
            resultIntent = entityFacade.getDetailIntent(entity, appContext);
            resultIntent.setData(entity.getUri());
        } else {
            resultIntent = new Intent(context, MainActivity_.class);
            resultIntent.setAction(MainActivityFragments.VMS.name());
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        notificationHelper.showTriggersNotification(
                entitiesAndTriggers, context, PendingIntent.getActivity(context, 0, resultIntent, 0)
        );
    }

    private void sendSyncIntent(boolean sync) {
        inSync = sync;
        Intent intent = new Intent(Broadcasts.IN_SYNC);
        intent.putExtra(Broadcasts.Extras.SYNCING, sync);
        context.sendBroadcast(intent);
    }
}
