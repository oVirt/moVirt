package org.ovirt.mobile.movirt.util.preferences;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.res.BooleanRes;
import org.androidannotations.annotations.res.IntegerRes;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.provider.OVirtContract;

@EBean(scope = EBean.Scope.Singleton)
public class SharedPreferencesHelper {

    private static final int SECONDS_IN_MINUTE = 60;

    private SharedPreferences sharedPreferences;

    @App
    MoVirtApp app;

    @Bean
    MovirtAuthenticator authenticator;

    @BooleanRes
    boolean defaultConnectionNotification;

    @BooleanRes
    boolean defaultPollEvents;

    @BooleanRes
    boolean defaultPeriodicSync;

    @IntegerRes
    int defaultPeriodicSyncInterval;

    @IntegerRes
    int defaultMaxEvents;

    @IntegerRes
    int defaultMaxVms;

    @StringRes
    String defaultEventsSearchQuery;

    @StringRes
    String defaultVmsSearchQuery;

    @AfterInject
    void initialize() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
    }

    public Boolean getBooleanPref(SettingsKey key) {
        switch (key) {
            case CONNECTION_NOTIFICATION:
                return sharedPreferences.getBoolean(key.getValue(), defaultConnectionNotification);
            case POLL_EVENTS:
                return sharedPreferences.getBoolean(key.getValue(), defaultPollEvents);
            case PERIODIC_SYNC:
                return sharedPreferences.getBoolean(key.getValue(), defaultPeriodicSync);
            default:
                return null;
        }
    }

    public Integer getIntPref(SettingsKey key) {
        switch (key) {
            case PERIODIC_SYNC_INTERVAL:
            case MAX_EVENTS:
            case MAX_VMS:
                return Integer.parseInt(getStringPref(key));
            default:
                return null;
        }
    }

    public String getStringPref(SettingsKey key) {
        switch (key) {
            case PERIODIC_SYNC_INTERVAL:
                return sharedPreferences.getString(key.getValue(), Integer.toString(defaultPeriodicSyncInterval));
            case MAX_EVENTS:
                return sharedPreferences.getString(key.getValue(), Integer.toString(defaultMaxEvents));
            case MAX_VMS:
                return sharedPreferences.getString(key.getValue(), Integer.toString(defaultMaxVms));
            case EVENTS_SEARCH_QUERY:
                return sharedPreferences.getString(key.getValue(), defaultEventsSearchQuery);
            case VMS_SEARCH_QUERY:
                return sharedPreferences.getString(key.getValue(), defaultVmsSearchQuery);
            default:
                return null;
        }
    }

    public int getPeriodicSyncInterval() {
        return getIntPref(SettingsKey.PERIODIC_SYNC_INTERVAL);
    }

    public int getMaxEvents() {
        return getIntPref(SettingsKey.MAX_EVENTS);
    }

    public int getMaxVms() {
        return getIntPref(SettingsKey.MAX_VMS);
    }

    public boolean isPollEventsEnabled() {
        return getBooleanPref(SettingsKey.POLL_EVENTS);
    }

    public boolean isConnectionNotificationEnabled() {
        return getBooleanPref(SettingsKey.CONNECTION_NOTIFICATION);
    }

    public void updatePeriodicSync() {
        Account account = authenticator.getAccount();
        String authority = OVirtContract.CONTENT_AUTHORITY;
        Bundle bundle = Bundle.EMPTY;

        if (getBooleanPref(SettingsKey.PERIODIC_SYNC)) {
            long intervalInSeconds = (long) getIntPref(SettingsKey.PERIODIC_SYNC_INTERVAL) * (long) SECONDS_IN_MINUTE;
            ContentResolver.addPeriodicSync(account, authority, bundle, intervalInSeconds);
        } else {
            ContentResolver.removePeriodicSync(account, authority, bundle);
        }
    }
}
