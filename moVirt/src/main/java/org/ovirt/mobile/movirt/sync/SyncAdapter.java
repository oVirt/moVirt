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

        try {
            updateClusters();
            updateHosts();
            updateVms();
            updateDataCenters();
            updateStorageDomains();

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

    public <E extends OVirtEntity> void syncEntity(final Class<E> clazz,
                                                                OVirtClient.Request<E> request,
                                                                OVirtClient.Response<E> response) {
        final EntityFacade<E> entityFacade = entityFacadeLocator.getFacade(clazz);
        final ProviderFacade.BatchBuilder batch = provider.batch();
        oVirtClient.fireRestRequest(request, new OVirtClient.CompositeResponse<>(new SyncResponse<E>() {
            @Override
            public void onResponse(E entity) throws RemoteException {
                Collection<Trigger<E>> allTriggers = entityFacade.getAllTriggers();
                updateLocalEntity(entity, clazz, allTriggers, batch);
                applyBatch(batch);
            }
        }, response));
    }

    /** Sends broadcasts about start/stop of sync around response */
    private class SyncResponse<T> extends OVirtClient.SimpleResponse<T> {
        @Override
        public void before() {
            sendSyncIntent(true);
        }

        @Override
        public void after() {
            sendSyncIntent(false);
        }
    }

    private void updateClusters() {
        oVirtClient.getClusters(new SyncResponse<List<Cluster>>() {
            @Override
            public void onResponse(List<Cluster> clusters) throws RemoteException {
                updateLocalEntities(clusters, Cluster.class);
            }
        });
    }

    private void updateVms() {
        oVirtClient.getVms(new SyncResponse<List<Vm>>() {
            @Override
            public void onResponse(List<Vm> vms) throws RemoteException {
                updateLocalEntities(vms, Vm.class);
            }
        });
    }

    private void updateHosts() {
        oVirtClient.getHosts(new SyncResponse<List<Host>>() {
            @Override
            public void onResponse(List<Host> hosts) throws RemoteException {
                updateLocalEntities(hosts, Host.class);
            }
        });
    }

    private void updateDataCenters() {
        oVirtClient.getDataCenters(new SyncResponse<List<DataCenter>>() {
            @Override
            public void onResponse(List<DataCenter> dataCenters) throws RemoteException {
                updateLocalEntities(dataCenters, DataCenter.class);
            }
        });
    }

    private void updateStorageDomains() {
        oVirtClient.getStorageDomains(new SyncResponse<List<StorageDomain>>() {
            @Override
            public void onResponse(List<StorageDomain> storageDomains) throws RemoteException {
                updateLocalEntities(storageDomains, StorageDomain.class);
            }
        });
    }

    private void applyBatch(ProviderFacade.BatchBuilder batch) {
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

        while (cursor.moveToNext()) {
            E localEntity = mapper.fromCursor(cursor);
            E remoteEntity = entityMap.get(localEntity.getId());
            if (remoteEntity == null) { // local entity obsolete, schedule delete from db
                Log.i(TAG, "Scheduling delete for URI" + localEntity.getUri());
                batch.delete(localEntity);
            } else { // existing entity, update stats if changed
                entityMap.remove(localEntity.getId());
                checkEntityChanged(localEntity, remoteEntity, entityFacade, allTriggers, batch);
            }
        }

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

    private <E extends OVirtEntity> void checkEntityChanged(E localEntity, E remoteEntity, EntityFacade<E> entityFacade,
                                                            Collection<Trigger<E>> allTriggers, ProviderFacade.BatchBuilder batch) {
        if (!localEntity.equals(remoteEntity)) {
            if (entityFacade != null) {
                final List<Trigger<E>> triggers = entityFacade.getTriggers(localEntity, allTriggers);
                processEntityTriggers(triggers, localEntity, remoteEntity, entityFacade);
            }
            Log.i(TAG, "Scheduling update for URI: " + localEntity.getUri());
            batch.update(remoteEntity);
        }
    }

    private <E extends OVirtEntity> void processEntityTriggers(List<Trigger<E>> triggers, E localEntity, E remoteEntity, EntityFacade<E> entityFacade) {
        Log.i(TAG, "Processing triggers for entity: " + remoteEntity.getId());
        for (Trigger<E> trigger : triggers) {
            if (!trigger.getCondition().evaluate(localEntity) && trigger.getCondition().evaluate(remoteEntity)) {
                displayNotification(trigger, remoteEntity, entityFacade);
            }
        }
    }

    private <E extends OVirtEntity> void displayNotification(Trigger<E> trigger, E entity, EntityFacade<E> entityFacade) {
        final Context appContext = getContext().getApplicationContext();
        final Intent intent = entityFacade.getDetailIntent(entity, appContext);
        intent.setData(entity.getUri());
        notificationHelper.showTriggerNotification(
                trigger, entity, appContext, PendingIntent.getActivity(appContext, 0, intent, 0)
        );
    }

    private void sendSyncIntent(boolean syncing) {
        inSync = syncing;
        Intent intent = new Intent(Broadcasts.IN_SYNC);
        intent.putExtra(Broadcasts.Extras.SYNCING, syncing);
        context.sendBroadcast(intent);
    }

}
