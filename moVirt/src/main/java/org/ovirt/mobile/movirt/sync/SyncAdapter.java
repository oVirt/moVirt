package org.ovirt.mobile.movirt.sync;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolverFactory;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.VmDetailActivity_;
import org.ovirt.mobile.movirt.util.NotificationDisplayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    @RootContext
    Context context;

    @SystemService
    NotificationManager notificationManager;

    @SystemService
    Vibrator vibrator;

    @Bean
    OVirtClient oVirtClient;

    @Bean
    ProviderFacade provider;

    @Bean
    TriggerResolverFactory triggerResolverFactory;

    @Bean
    EventsHandler eventsHandler;

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    NotificationDisplayer notificationDisplayer;

    public static volatile boolean inSync = false;

    /** access to the {@code batch} field should be always under synchronized(this) */
    ProviderFacade.BatchBuilder batch;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient providerClient, SyncResult syncResult) {
        doPerformSync(true);
    }

    public synchronized void doPerformSync(boolean tryEvents) {
        if (inSync) {
            return;
        }

        if (!authenticator.accountConfigured()) {
            Log.d(TAG, "Account not configured, not performing sync");
            return;
        }

        sendSyncIntent(true);
        try {
            // split to two methods so at least the quick entities can be already shown / used until the slow ones get processed (better ux)
            updateQuickEntities();
            if (tryEvents) {
                eventsHandler.updateEvents(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating data", e);
            Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
            intent.putExtra(Broadcasts.Extras.CONNECTION_FAILURE_REASON, e.getMessage());
            context.sendBroadcast(intent);
        }
    }

    public synchronized void syncVm(final String id, final OVirtClient.Response<Vm> response) {
        initBatch();
        oVirtClient.getVm(id, new OVirtClient.CompositeResponse<>(new OVirtClient.SimpleResponse<Vm>() {
            @Override
            public void onResponse(Vm vm) throws RemoteException {
                updateLocalEntity(vm, Vm.class);
                applyBatch();
            }
        }, response));
    }

    private void updateQuickEntities() throws RemoteException {
        initBatch();

        oVirtClient.getVms(new OVirtClient.SimpleResponse<List<Vm>>() {
            @Override
            public void onResponse(final List<Vm> remoteVms) throws RemoteException {
                oVirtClient.getClusters(new OVirtClient.SimpleResponse<List<Cluster>>() {
                    @Override
                    public void onResponse(List<Cluster> remoteClusters) throws RemoteException {
                        updateLocalEntities(remoteClusters, Cluster.class);
                        updateLocalEntities(remoteVms, Vm.class);

                        applyBatch();
                    }
                });
            }

            @Override
            public void after() {
                sendSyncIntent(false);
            }
        });

    }

    private void initBatch() {
        batch = provider.batch();
    }

    private void applyBatch() {
        if (batch.isEmpty()) {
            Log.i(TAG, "No updates necessary");
        } else {
            Log.i(TAG, "Applying batch update");
            batch.apply();
        }
    }

    private <E extends OVirtEntity> void updateLocalEntities(List<E> remoteEntities, Class<E> clazz)
            throws RemoteException {
        final Map<String, E> entityMap = groupEntitiesById(remoteEntities);
        final EntityMapper<E> mapper = EntityMapper.forEntity(clazz);
        final TriggerResolver<E> triggerResolver = triggerResolverFactory.getResolverForEntity(clazz);

        final Cursor cursor = provider.query(clazz).asCursor();
        while (cursor.moveToNext()) {
            E localEntity = mapper.fromCursor(cursor);
            E remoteEntity = entityMap.get(localEntity.getId());
            if (remoteEntity == null) { // local entity obsolete, schedule delete from db
                Log.i(TAG, "Scheduling delete for URI" + localEntity.getUri());
                batch.delete(localEntity);
            } else { // existing entity, update stats if changed
                entityMap.remove(localEntity.getId());
                checkEntityChanged(localEntity, remoteEntity, triggerResolver);
            }
        }

        for (E entity : entityMap.values()) {
            Log.i(TAG, "Scheduling insert for entity: id = " + entity.getId());
            batch.insert(entity);
        }
    }

    private <E extends OVirtEntity> void updateLocalEntity(E remoteEntity, Class<E> clazz) {
        final TriggerResolver<E> triggerResolver = triggerResolverFactory.getResolverForEntity(clazz);

        Collection<E> localEntities = provider.query(clazz).id(remoteEntity.getId()).all();
        if (localEntities.isEmpty()) {
            Log.i(TAG, "Scheduling insert for entity: id = " + remoteEntity.getId());
            batch.insert(remoteEntity);
        } else {
            E localEntity = localEntities.iterator().next();
            checkEntityChanged(localEntity, remoteEntity, triggerResolver);
        }
    }

    private <E extends OVirtEntity> void checkEntityChanged(E localEntity, E remoteEntity, TriggerResolver<E> triggerResolver) {
        if (!localEntity.equals(remoteEntity)) {
            if (triggerResolver != null) {
                final List<Trigger<E>> triggers = triggerResolver.getTriggersForEntity(localEntity);
                processEntityTriggers(triggers, localEntity, remoteEntity);
            }
            Log.i(TAG, "Scheduling update for URI: " + localEntity.getUri());
            batch.update(remoteEntity);
        }
    }

    private <E extends OVirtEntity> void processEntityTriggers(List<Trigger<E>> triggers, E localEntity, E remoteEntity) {
        Log.i(TAG, "Processing triggers for entity: " + remoteEntity.getId());
        for (Trigger<E> trigger : triggers) {
            if (!trigger.getCondition().evaluate(localEntity) && trigger.getCondition().evaluate(remoteEntity)) {
                displayNotification(trigger, remoteEntity);
            }
        }
    }

    // TODO: generalize to multiple entity types
    private <E extends OVirtEntity> void displayNotification(Trigger<E> trigger, E entity) {
        final Context appContext = getContext().getApplicationContext();
        final Intent intent = new Intent(appContext, VmDetailActivity_.class);
        intent.setData(entity.getUri());
        notificationDisplayer.showNotification(
                trigger, entity, appContext, PendingIntent.getActivity(appContext, 0, intent, 0)
        );
    }

    private static <E extends OVirtEntity> Map<String, E> groupEntitiesById(List<E> entities) {
        Map<String, E> entityMap = new HashMap<>();
        for (E entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
        return entityMap;
    }


    private void sendSyncIntent(boolean syncing) {
        inSync = syncing;
        Intent intent = new Intent(Broadcasts.IN_SYNC);
        intent.putExtra(Broadcasts.Extras.SYNCING, syncing);
        context.sendBroadcast(intent);
    }

}
