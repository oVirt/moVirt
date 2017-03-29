package org.ovirt.mobile.movirt.sync;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.trigger.EventTriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.EventProviderHelper;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@EBean(scope = EBean.Scope.Singleton)
public class EventsHandler {
    private static final String TAG = EventsHandler.class.getSimpleName();
    private static AtomicBoolean inSync = new AtomicBoolean();

    @Bean
    ProviderFacade provider;

    @Bean
    EventProviderHelper eventProviderFacade;

    @Bean
    OVirtClient oVirtClient;
    @Bean
    EventTriggerResolver eventTriggerResolver;
    @Bean
    NotificationHelper notificationHelper;
    @RootContext
    Context context;
    @Bean
    SharedPreferencesHelper sharedPreferencesHelper;
    @Bean
    MessageHelper messageHelper;

    public void syncAllEvents() {
        syncAllEvents(null);
    }

    public void syncAllEvents(Response<List<Event>> response) {
        try {
            if (!inSync.compareAndSet(false, true)) {
                return;
            }

            Log.d(TAG, "Syncing all events");
            oVirtClient.getEventsSince(eventProviderFacade.getLastEventId(), new CompositeResponse<>(new SimpleResponse<List<Event>>() {

                @Override
                public void onResponse(List<Event> newEvents) throws RemoteException {
                    updateEvents(newEvents);
                    discardOldEvents();
                }

                @Override
                public void after() {
                    inSync.set(false);
                }
            }, response));
        } catch (Exception e) {
            inSync.set(false);
            messageHelper.showError(e);
        }
    }

    public void syncHostEvents(Response<List<Event>> response, final String hostId, final String hostName) {
        Log.d(TAG, String.format("Syncing  events for host: %s; %s", hostName, hostId));
        oVirtClient.getHostEvents(hostName, new CompositeResponse<>(new SimpleResponse<List<Event>>() {

            @Override
            public void onResponse(List<Event> newEvents) throws RemoteException {
                updateEntityEvents(newEvents, hostId, null, null);
            }
        }, response));
    }

    public void syncVmEvents(Response<List<Event>> response, final String vmId, final String vmName) {
        Log.d(TAG, String.format("Syncing  events for vm: %s; %s", vmName, vmId));
        oVirtClient.getVmEvents(vmName, new CompositeResponse<>(new SimpleResponse<List<Event>>() {

            @Override
            public void onResponse(List<Event> newEvents) throws RemoteException {
                updateEntityEvents(newEvents, null, vmId, null);
            }
        }, response));
    }

    public void syncStorageDomainEvents(Response<List<Event>> response, final String storageDomainId, final String storageDomainName) {
        Log.d(TAG, String.format("Syncing  events for storage domain: %s; %s", storageDomainName, storageDomainId));
        oVirtClient.getStorageDomainEvents(storageDomainName, new CompositeResponse<>(new SimpleResponse<List<Event>>() {

            @Override
            public void onResponse(List<Event> newEvents) throws RemoteException {
                updateEntityEvents(newEvents, null, null, storageDomainId);
            }
        }, response));
    }

    /**
     * @param newEvents events to be added to db
     */
    private void updateEvents(List<Event> newEvents) {
        discardTemporaryEvents();

        Pair<String[], SparseArray<Event>> idsAndMap = getIdsAndMap(newEvents);
        String[] polledIds = idsAndMap.first;
        SparseArray<Event> newEventsMap = idsAndMap.second;

        Cursor cursor = null;
        try {
            // get Ids of already persisted events which match downloaded ones
            cursor = provider.query(Event.class)
                    .whereIn(OVirtContract.Event.ID, polledIds)
                    .projection(new String[]{OVirtContract.Event.ID})
                    .asCursor();

            // filter already persisted events - we don't know entity ids like we do in updateEntityEvents()
            while (cursor.moveToNext()) {
                Integer localId = new CursorHelper(cursor).getInt(OVirtContract.Event.ID);
                newEventsMap.remove(localId);
            }
        } finally {
            ObjectUtils.closeSilently(cursor);
        }

        ProviderFacade.BatchBuilder batch = provider.batch();
        newEvents = new ArrayList<>(newEventsMap.size());

        for (int i = 0; i < newEventsMap.size(); i++) {
            Event event = newEventsMap.valueAt(i);
            batch.insert(event);
            newEvents.add(event);
        }

        applyBatch(batch, batch.size());
        processEventTriggers(newEvents);
    }

    /**
     * @param newEvents events to be added to db
     */
    private void updateEntityEvents(List<Event> newEvents, String hostId, String vmId, String storageDomainId) {
        Pair<String[], SparseArray<Event>> idsAndMap = getIdsAndMap(newEvents);
        String[] polledIds = idsAndMap.first;
        SparseArray<Event> newEventsMap = idsAndMap.second;

        // get already persisted entities
        final Collection<Event> localEvents = provider.query(Event.class)
                .whereIn(OVirtContract.Event.ID, polledIds)
                .all();

        ProviderFacade.BatchBuilder batch = provider.batch();

        // update changes - set missing ids
        // we update only the ids which we got as an argument, because there can be events which belong to more entities - host, vm, storage
        for (Event local : localEvents) {
            boolean update = refreshIds(local, hostId, vmId, storageDomainId);

            if (update) {
                batch.update(local);
            }
            newEventsMap.remove(local.getId());
        }

        for (int i = 0; i < newEventsMap.size(); i++) {
            Event event = newEventsMap.valueAt(i);
            refreshIds(event, hostId, vmId, storageDomainId); // precaution, so the ids are always set
            event.setTemporary(true);
            batch.insert(event);
        }

        applyBatch(batch, newEventsMap.size());
    }

    public void discardOldEvents() {
        try {
            eventProviderFacade.deleteEventsAndLetOnly(sharedPreferencesHelper.getMaxEventsStored());
        } catch (Exception e) {
            messageHelper.showError(e);
        }
    }

    public void discardTemporaryEvents() {
        try {
            eventProviderFacade.deleteTemporaryEvents();
        } catch (Exception e) {
            messageHelper.showError(e);
        }
    }

    private Pair<String[], SparseArray<Event>> getIdsAndMap(List<Event> newEvents) {
        String[] polledIds = new String[newEvents.size()];
        SparseArray<Event> newEventsMap = new SparseArray<>(newEvents.size());

        for (int i = 0; i < newEvents.size(); i++) {
            Event event = newEvents.get(i);
            Integer id = event.getId();

            polledIds[i] = id.toString();
            newEventsMap.put(id, event);
        }

        return new Pair<>(polledIds, newEventsMap);
    }

    private boolean refreshIds(Event event, String hostId, String vmId, String storageDomainId) {
        boolean update = false;

        if (hostId != null && !hostId.equals(event.getHostId())) {
            event.setHostId(hostId);
            update = true;
        }

        if (vmId != null && !vmId.equals(event.getVmId())) {
            event.setVmId(vmId);
            update = true;
        }

        if (storageDomainId != null && !storageDomainId.equals(event.getStorageDomainId())) {
            event.setStorageDomainId(storageDomainId);
            update = true;
        }
        return update;
    }

    /**
     * @param newEventsCount for logging purposes
     */
    private void applyBatch(ProviderFacade.BatchBuilder batch, int newEventsCount) {
        if (batch.isEmpty()) {
            Log.d(TAG, "No new events");
        } else {
            Log.d(TAG, String.format("Applying batch update, %d new event(s), updated  %d event(s)", newEventsCount
                    , batch.size() - newEventsCount));
            batch.apply();
        }
    }

    private void processEventTriggers(List<Event> events) {
        Collection<Trigger<Event>> allEventTriggers = eventTriggerResolver.getAllTriggers();
        List<Pair<Event, Trigger<Event>>> eventsAndTriggers = new ArrayList<>();

        for (Event event : events) {
            final List<Trigger<Event>> triggers = eventTriggerResolver.getTriggers(event, allEventTriggers);
            Log.d(TAG, "Processing triggers for Events: " + event.getId());
            for (Trigger<Event> trigger : triggers) {
                if (trigger.getCondition().evaluate(event)) {
                    eventsAndTriggers.add(new Pair<>(event, trigger));
                }
            }
        }
        displayNotification(eventsAndTriggers);
    }

    private void displayNotification(List<Pair<Event, Trigger<Event>>> eventsAndTriggers) {
        if (eventsAndTriggers.size() == 0) {
            return;
        }
        Intent resultIntent = new Intent(context, MainActivity_.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setAction(MainActivityFragments.EVENTS.name());
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        0
                );
        notificationHelper.showTriggersNotification(eventsAndTriggers, context, resultPendingIntent);
    }
}
