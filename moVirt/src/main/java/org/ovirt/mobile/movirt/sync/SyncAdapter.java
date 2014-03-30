package org.ovirt.mobile.movirt.sync;

import android.accounts.Account;
import android.app.Notification;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolverFactory;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.VmDetailActivity_;

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

    @Bean
    OVirtClient oVirtClient;

    @Bean
    ProviderFacade provider;

    @Bean
    TriggerResolverFactory triggerResolverFactory;

    int lastEventId = 0;
    int notificationCount;
    ProviderFacade.BatchBuilder batch;

    private boolean successfulConnection = false;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @AfterInject
    void initLastEventId() {
        lastEventId = provider.getLastEventId();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient providerClient, SyncResult syncResult) {
        Log.d(TAG, "Performing full sync for account[" + account.name + "]");

        try {
            final List<Vm> remoteVms = oVirtClient.getVms();
            final List<Cluster> remoteClusters = oVirtClient.getClusters();
            final List<Event> newEvents = oVirtClient.getEventsSince(lastEventId);

            batch = provider.batch();
            notificationCount = 0;
            updateLocalEntities(remoteClusters, Cluster.class);
            updateLocalEntities(remoteVms, Vm.class);
            updateEvents(newEvents);

            if (batch.isEmpty()) {
                Log.i(TAG, "No updates necessary");
            } else {
                Log.i(TAG, "Applying batch update");
                batch.apply();
            }

            if (!successfulConnection) {
                successfulConnection = true;
                context.sendBroadcast(new Intent(MoVirtApp.CONNECTION_SUCCESS));

            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating data", e);
            successfulConnection = false;
            context.sendBroadcast(new Intent(MoVirtApp.CONNECTION_FAILURE));
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
                if (!localEntity.equals(remoteEntity)) {
                    if (triggerResolver != null) {
                        final List<Trigger<E>> triggers = triggerResolver.getTriggersForEntity(localEntity);
                        processEntityTriggers(triggers, localEntity, remoteEntity);
                    }
                    Log.i(TAG, "Scheduling update for URI: " + localEntity.getUri());
                    batch.update(remoteEntity);
                }
            }
        }

        for (E entity : entityMap.values()) {
            Log.i(TAG, "Scheduling insert for entity: id = " + entity.getId());
            batch.insert(entity);
        }
    }

    private void updateEvents(List<Event> newEvents) {
        if (!newEvents.isEmpty()) {
            lastEventId = newEvents.get(0).getId();
        }
        Log.i(TAG, "Fetched " + newEvents.size() + " new event(s)");

        for (Event event : newEvents) {
            batch.insert(event);
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
        Log.d(TAG, "Displaying notification " + notificationCount);
        final Context appContext = getContext().getApplicationContext();
        final Intent intent = new Intent(appContext, VmDetailActivity_.class);
        intent.setData(entity.getUri());
        notificationManager.notify(notificationCount++, new NotificationCompat.Builder(appContext)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(org.ovirt.mobile.movirt.R.drawable.ic_launcher)
                        .setContentTitle(trigger.getNotificationType() == Trigger.NotificationType.INFO ? "oVirt event" : ">>> oVirt event <<<")
                        .setContentText(trigger.getCondition().getMessage(entity))
                        .setContentIntent(PendingIntent.getActivity(appContext, 0, intent, 0))
                        .build());
    }

    private static <E extends OVirtEntity> Map<String, E> groupEntitiesById(List<E> entities) {
        Map<String, E> entityMap = new HashMap<>();
        for (E entity : entities) {
            entityMap.put(entity.getId(), entity);
        }
        return entityMap;
    }

}
