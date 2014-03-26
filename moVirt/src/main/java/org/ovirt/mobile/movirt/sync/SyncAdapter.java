package org.ovirt.mobile.movirt.sync;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolverFactory;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.VmDetailActivity_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    @Bean
    OVirtClient oVirtClient;

    ContentProviderClient contentClient;

    @Bean
    ProviderFacade provider;

    @Bean
    TriggerResolverFactory triggerResolverFactory;

    int lastEventId = 0;

    public SyncAdapter(Context context) {
        super(context, true);
        contentClient = context.getContentResolver().acquireContentProviderClient(OVirtContract.BASE_CONTENT_URI);
    }

    @AfterInject
    void initLastEventId() {
        lastEventId = provider.getLastEventId();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Performing full sync for account[" + account.name + "]");

        try {
            final List<Vm> remoteVms = oVirtClient.getVms();
            final List<Cluster> remoteClusters = oVirtClient.getClusters();
            final List<Event> newEvents = oVirtClient.getEventsSince(lastEventId);

            final ArrayList<ContentProviderOperation> batch = new ArrayList<>();
            batch.addAll(updateLocalEntities(OVirtContract.Cluster.CONTENT_URI, remoteClusters, Cluster.class));
            batch.addAll(updateLocalEntities(OVirtContract.Vm.CONTENT_URI, remoteVms, Vm.class));
            batch.addAll(updateEvents(newEvents));

            if (batch.isEmpty()) {
                Log.i(TAG, "No updates necessary");
            } else {
                Log.i(TAG, "Applying batch update");
                contentClient.applyBatch(batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <E extends OVirtEntity> List<ContentProviderOperation> updateLocalEntities(Uri baseContentUri, List<E> remoteEntities, Class<E> clazz)
            throws RemoteException {
        final ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        final Map<String, E> entityMap = groupEntitiesById(remoteEntities);
        final EntityMapper<E> mapper = EntityMapper.forEntity(clazz);
        final TriggerResolver<E> triggerResolver = triggerResolverFactory.getResolverForEntity(clazz);

        final Cursor cursor = provider.query(clazz).asCursor();
        while (cursor.moveToNext()) {
            E localEntity = mapper.fromCursor(cursor);
            E remoteEntity = entityMap.get(localEntity.getId());
            if (remoteEntity == null) { // local entity obsolete, schedule delete from db
                Uri deleteUri = localEntity.getUri();
                Log.i(TAG, "Scheduling delete for URI: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
            } else { // existing entity, update stats if changed
                entityMap.remove(localEntity.getId());
                if (!localEntity.equals(remoteEntity)) {
                    if (triggerResolver != null) {
                        final List<Trigger<E>> triggers = triggerResolver.getTriggersForEntity(localEntity);
                        processEntityTriggers(triggers, localEntity, remoteEntity);
                    }
                    Uri existingUri = localEntity.getUri();
                    Log.i(TAG, "Scheduling update for URI: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri).withValues(remoteEntity.toValues()).build());
                }
            }
        }

        for (E entity : entityMap.values()) {
            Log.i(TAG, "Scheduling insert for entity: id = " + entity.getId());
            batch.add(ContentProviderOperation.newInsert(baseContentUri).withValues(entity.toValues()).build());
        }

        return batch;
    }

    private List<ContentProviderOperation> updateEvents(List<Event> newEvents) {
        if (!newEvents.isEmpty()) {
            lastEventId = newEvents.get(0).getId();
        }
        Log.i(TAG, "Fetched " + newEvents.size() + " new event(s)");

        List<ContentProviderOperation> batch = new ArrayList<>();
        for (Event event : newEvents) {
            batch.add(ContentProviderOperation.newInsert(OVirtContract.Event.CONTENT_URI).withValues(event.toValues()).build());
        }

        return batch;
    }

    private <E extends OVirtEntity> void processEntityTriggers(List<Trigger<E>> triggers, E localEntity, E remoteEntity) {
        Log.i(TAG, "Processing triggers for entity: " + remoteEntity.getId());
        int i = 0;
        for (Trigger<E> trigger : triggers) {
            Log.d(TAG, "Displaying notification " + i);
            if (!trigger.getCondition().evaluate(localEntity) && trigger.getCondition().evaluate(remoteEntity)) {
                displayNotification(i++, trigger, remoteEntity);
            }
        }
    }

    // TODO: generalize to multiple entity types
    private <E extends OVirtEntity> void displayNotification(int i, Trigger<E> trigger, E entity) {
        final Context appContext = getContext().getApplicationContext();
        final Intent intent = new Intent(appContext, VmDetailActivity_.class);
        intent.setData(entity.getUri());
        ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(i, new NotificationCompat.Builder(appContext)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(org.ovirt.mobile.movirt.R.drawable.ic_launcher)
                        .setContentTitle(trigger.getNotificationType() == Trigger.NotificationType.INFO ? "oVirt event" : ">>> oVirt event <<<")
                        .setContentText(trigger.getCondition().toString())
                        .setContentIntent(PendingIntent.getActivity(appContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK))
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
