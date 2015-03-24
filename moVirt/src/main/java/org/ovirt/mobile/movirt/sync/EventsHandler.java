package org.ovirt.mobile.movirt.sync;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolverFactory;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.ui.TabChangedListener;
import org.ovirt.mobile.movirt.util.NotificationDisplayer;

import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class EventsHandler implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = EventsHandler.class.getSimpleName();

    public static final boolean DEFAULT_POLL_EVENTS = true;

    public static String MAX_EVENTS_LOCALLY = "500";

    private int maxEventsStored = -1;

    private boolean deleteEventsBeforeInsert = false;

    public static volatile boolean inSync = false;

    @App
    MoVirtApp app;

    @SystemService
    NotificationManager notificationManager;

    @SystemService
    Vibrator vibrator;

    @Bean
    ProviderFacade provider;

    @Bean
    OVirtClient oVirtClient;

    @Bean
    TriggerResolverFactory triggerResolverFactory;

    @Bean
    NotificationDisplayer notificationDisplayer;

    @RootContext
    Context context;

    ProviderFacade.BatchBuilder batch;

    int lastEventId = 0;

    @AfterInject
    void initLastEventId() {
        lastEventId = provider.getLastEventId();
    }

    @AfterInject
    void initialize() {
        PreferenceManager.getDefaultSharedPreferences(app).registerOnSharedPreferenceChangeListener(this);
    }

    public void updateEvents(boolean force) {
        // it is not exactly thread safe - there is a small chance that two syncs will happen in parallel.
        // but it does not cause anything worse than just that the same events will be downloaded twice
        if (inSync) {
            return;
        }

        try {
            boolean configuredPoll = PreferenceManager.getDefaultSharedPreferences(app).getBoolean("poll_events", DEFAULT_POLL_EVENTS);

            if (configuredPoll || force) {
                batch = provider.batch();
                oVirtClient.getEventsSince(!deleteEventsBeforeInsert ? lastEventId : 0, new OVirtClient.SimpleResponse<List<Event>>() {

                    @Override
                    public void before() {
                        inSync = true;
                        sendSyncIntent(true);
                    }

                    @Override
                    public void onResponse(List<Event> newEvents) throws RemoteException {
                        updateEvents(newEvents);
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
            lastEventId = 0;
            // no need to do it again
            deleteEventsBeforeInsert = false;
        }
    }

    private void updateEvents(List<Event> newEvents) {
        Log.i(TAG, "Fetched " + newEvents.size() + " new event(s)");
        if (deleteEventsBeforeInsert) {
            deleteEvents();
        }

        final TriggerResolver<Event> triggerResolver = triggerResolverFactory.getResolverForEntity(Event.class);

        int newLastEventCandidate = -1;

        for (Event event : newEvents) {
            // because the user api (filtered: true) returns all the events all the time
            if (event.getId() > lastEventId) {
                this.processEventTriggers(event, triggerResolver);
                batch.insert(event);
                if(event.getId() > newLastEventCandidate) {
                    newLastEventCandidate = event.getId();
                }
            }
        }

        if (newLastEventCandidate > lastEventId) {
            lastEventId = newLastEventCandidate;
        }
    }

    private void processEventTriggers(Event event, TriggerResolver<Event> triggerResolver) {
        final List<Trigger<Event>> triggers = triggerResolver.getTriggersForEntity(event);
        Log.i(TAG, "Processing triggers for Event: " + event.getId());
        for (Trigger<Event> trigger : triggers) {
            if (trigger.getCondition().evaluate(event)) {
                displayNotification(trigger, event);
            }
        }
    }

    private void displayNotification(Trigger<Event> trigger, Event event) {
        final Context appContext = context.getApplicationContext();
        final Intent intent = new Intent(appContext, MainActivity_.class);
        intent.putExtra(MainActivity_.EXTRA_ACTIVE_TAB, TabChangedListener.CurrentlyShown.EVENTS.toString());
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
        stackBuilder.addParentStack(MainActivity_.class);
        stackBuilder.addNextIntent(intent);
        final PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationDisplayer.showNotification(trigger, event, appContext, resultPendingIntent);
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
            maxEventsStored = getMaxEvents();
        }

        provider.deleteEventsAndLetOnly(maxEventsStored);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("max_events_stored")) {
            int newMaxEventsStored = getMaxEvents();
            if (newMaxEventsStored > maxEventsStored) {
                deleteEventsBeforeInsert = true;
            }

            maxEventsStored = newMaxEventsStored;
        }
    }

    private int getMaxEvents() {
        String maxEventsString = PreferenceManager.getDefaultSharedPreferences(app).getString("max_events_stored", MAX_EVENTS_LOCALLY);
        try {
            return Integer.parseInt(maxEventsString);
        } catch (NumberFormatException e) {
            return Integer.parseInt(MAX_EVENTS_LOCALLY);
        }
    }

    private void sendSyncIntent(boolean syncing) {
        Intent intent = new Intent(Broadcasts.EVENTS_IN_SYNC);
        intent.putExtra(Broadcasts.Extras.SYNCING, syncing);
        context.sendBroadcast(intent);
    }

}
