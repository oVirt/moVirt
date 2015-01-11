package org.ovirt.mobile.movirt.sync;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class EventsHandler implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = EventsHandler.class.getSimpleName();

    public static final boolean DEFAULT_POLL_EVENTS = true;

    public static String MAX_EVENTS_LOCALLY = "500";

    private int maxEventsStored = -1;

    private boolean deleteEventsBeforeInsert = false;

    private static volatile boolean inSync = false;

    @App
    MoVirtApp app;

    @Bean
    ProviderFacade provider;

    @Bean
    OVirtClient oVirtClient;

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
        if (inSync) {
            return;
        }

        inSync = true;
        try {
            boolean configuredPoll = PreferenceManager.getDefaultSharedPreferences(app).getBoolean("poll_events", DEFAULT_POLL_EVENTS);

            if (configuredPoll || force) {
                batch = provider.batch();
                final List<Event> newEvents = oVirtClient.getEventsSince(!deleteEventsBeforeInsert ? lastEventId : 0);
                updateEvents(newEvents);
                applyBatch();

                deleteOldEvents();
            }
        } finally {
            inSync = false;
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

        int newLastEventCandidate = -1;

        for (Event event : newEvents) {
            // because the user api (filtered: true) returns all the events all the time
            if (event.getId() > lastEventId) {
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
}
