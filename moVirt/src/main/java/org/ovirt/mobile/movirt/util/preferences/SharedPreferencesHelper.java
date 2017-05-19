package org.ovirt.mobile.movirt.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.BooleanRes;
import org.androidannotations.annotations.res.IntegerRes;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.util.ObjectUtils;

@EBean
public class SharedPreferencesHelper implements AccountEnvironment.EnvDisposable {

    @RootContext
    Context context;

    @BooleanRes
    boolean defaultConnectionNotification;

    @BooleanRes
    boolean defaultPollEvents;

    @BooleanRes
    boolean defaultPeriodicSync;

    @IntegerRes
    int defaultPeriodicSyncInterval;

    @IntegerRes
    int defaultMaxEventsPolled;

    @IntegerRes
    int defaultMaxEventsStored;

    @IntegerRes
    int defaultMaxVms;

    @StringRes
    String defaultEventsSearchQuery;

    @StringRes
    String defaultVmsSearchQuery;

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper initialize(MovirtAccount account) {
        ObjectUtils.requireNotNull(account, "MovirtAccount");
        sharedPreferences = context.getSharedPreferences(account.getId() + Constants.PREFERENCES_NAME_SUFFIX, Context.MODE_PRIVATE);
        return this;
    }

    @Override
    public void dispose() {
        sharedPreferences.edit().clear().apply();
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
            case MAX_EVENTS_POLLED:
            case MAX_EVENTS_STORED:
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
            case MAX_EVENTS_POLLED:
                return sharedPreferences.getString(key.getValue(), Integer.toString(defaultMaxEventsPolled));
            case MAX_EVENTS_STORED:
                return sharedPreferences.getString(key.getValue(), Integer.toString(defaultMaxEventsStored));
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

    public boolean isPeriodicSyncEnabled() {
        return getBooleanPref(SettingsKey.PERIODIC_SYNC);
    }

    public int getPeriodicSyncInterval() {
        return getIntPref(SettingsKey.PERIODIC_SYNC_INTERVAL);
    }

    public int getMaxEventsPolled() {
        return getIntPref(SettingsKey.MAX_EVENTS_POLLED);
    }

    public int getMaxEventsStored() {
        return getIntPref(SettingsKey.MAX_EVENTS_STORED);
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
}
