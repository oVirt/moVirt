package org.ovirt.mobile.movirt.sync;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.trigger.EventTriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class EventsHandler {
    private static final String TAG = EventsHandler.class.getSimpleName();
    public static volatile boolean inSync = false;
    @Bean
    ProviderFacade provider;
    @Bean
    OVirtClient oVirtClient;
    @Bean
    EventTriggerResolver eventTriggerResolver;
    @Bean
    NotificationHelper notificationHelper;
    @RootContext
    Context context;
    ProviderFacade.BatchBuilder batch;
    @Bean
    SharedPreferencesHelper sharedPreferencesHelper;

    private int maxEventsStored = -1;
    private boolean deleteEventsBeforeInsert = false;

    @AfterInject
    void initialize() {
        maxEventsStored = sharedPreferencesHelper.getMaxEvents();
    }

    public void updateEvents(boolean force) {
        // it is not exactly thread safe - there is a small chance that two syncs will happen in parallel.
        // but it does not cause anything worse than just that the same events will be downloaded twice
        if (inSync) {
            return;
        }

        try {
            boolean configuredPoll = sharedPreferencesHelper.isPollEventsEnabled();

            if (configuredPoll || force) {
                batch = provider.batch();

                final int lastEventId = deleteEventsBeforeInsert ? 0 : provider.getLastEventId();

                oVirtClient.getEventsSince(!deleteEventsBeforeInsert ? lastEventId : 0, new SimpleResponse<List<Event>>() {

                    @Override
                    public void before() {
                        inSync = true;
                        sendSyncIntent(true);
                    }

                    @Override
                    public void onResponse(List<Event> newEvents) throws RemoteException {
                        updateEvents(newEvents, lastEventId);
                        applyBatch();

                        deleteOldEvents();
                    }

                    @Override
                    public void after() {
                        inSync = false;
                        sendSyncIntent(false);
                    }
                });

            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading events", e);
            Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
            intent.putExtra(Broadcasts.Extras.FAILURE_REASON, e.getMessage());
            context.sendBroadcast(intent);
        }
    }

    public void deleteEvents() {
        if (provider.deleteEvents() != -1) {
            // no need to do it again
            deleteEventsBeforeInsert = false;
        }
    }

    private void updateEvents(List<Event> newEvents, int lastEventId) {
        Log.i(TAG, "Fetched " + newEvents.size() + " new event(s)");
        if (deleteEventsBeforeInsert) {
            deleteEvents();
        }

        int newLastEventCandidate = -1;
        List<Event> filteredEvents = new ArrayList<>();

        for (Event event : newEvents) {
            // because the user api (filtered: true) returns all the events all the time
            if (event.getId() > lastEventId) {
                filteredEvents.add(event);
                batch.insert(event);
                if (event.getId() > newLastEventCandidate) {
                    newLastEventCandidate = event.getId();
                }
            }
        }

        this.processEventsTriggers(filteredEvents);
    }

    private void processEventsTriggers(List<Event> events) {
        Collection<Trigger<Event>> allEventTriggers = eventTriggerResolver.getAllTriggers();
        List<Pair<Event, Trigger<Event>>> eventsAndTriggers = new ArrayList<>();

        for (Event event : events) {
            final List<Trigger<Event>> triggers = eventTriggerResolver.getTriggers(event, allEventTriggers);
            Log.i(TAG, "Processing triggers for Events: " + event.getId());
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

    private void applyBatch() {
        if (batch.isEmpty()) {
            Log.i(TAG, "No updates necessary");
        } else {
            Log.i(TAG, "Applying batch update");
            batch.apply();
        }
    }

    private void deleteOldEvents() {
        if (maxEventsStored == -1) {
            maxEventsStored = sharedPreferencesHelper.getMaxEvents();
        }

        provider.deleteEventsAndLetOnly(maxEventsStored);
    }

    public void setMaxEventsStored(int newValue) {
        if (newValue > maxEventsStored) {
            deleteEventsBeforeInsert = true;
        }
        maxEventsStored = newValue;
    }

    private void sendSyncIntent(boolean syncing) {
        Intent intent = new Intent(Broadcasts.EVENTS_IN_SYNC);
        intent.putExtra(Broadcasts.Extras.SYNCING, syncing);
        context.sendBroadcast(intent);
    }

}
