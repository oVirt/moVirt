package org.ovirt.mobile.movirt.sync;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

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
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;

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

                oVirtClient.getEventsSince(!deleteEventsBeforeInsert ? lastEventId : 0, new OVirtClient.SimpleResponse<List<Event>>() {

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
            intent.putExtra(Broadcasts.Extras.CONNECTION_FAILURE_REASON, e.getMessage());
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

        Collection<Trigger<Event>> allEventTriggers = eventTriggerResolver.getAllTriggers();

        for (Event event : newEvents) {
            // because the user api (filtered: true) returns all the events all the time
            if (event.getId() > lastEventId) {
                this.processEventTriggers(event, allEventTriggers);
                batch.insert(event);
                if (event.getId() > newLastEventCandidate) {
                    newLastEventCandidate = event.getId();
                }
            }
        }
    }

    private void processEventTriggers(Event event, Collection<Trigger<Event>> allEventTriggers) {
        final List<Trigger<Event>> triggers = eventTriggerResolver.getTriggers(event, allEventTriggers);
        Log.i(TAG, "Processing triggers for Event: " + event.getId());
        for (Trigger<Event> trigger : triggers) {
            if (trigger.getCondition().evaluate(event)) {
                displayNotification(trigger, event);
            }
        }
    }

    private void displayNotification(Trigger<Event> trigger, Event event) {
        Intent resultIntent = new Intent(context, MainActivity_.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        0
                );
        notificationHelper.showTriggerNotification(trigger, event, context, resultPendingIntent);
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
