package org.ovirt.mobile.movirt.ui;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.sync.EventsHandler;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@EActivity
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_CONNECTION_NOTIFICATION = "connection_notification";
    public static final String KEY_PERIODIC_SYNC = "periodic_sync";
    public static final String KEY_PERIODIC_SYNC_INTERVAL = "periodic_sync_interval";
    public static final String KEY_MAX_EVENTS = "max_events_stored";
    public static final String KEY_MAX_VMS = "max_vms_polled";
    public static final String KEY_POLL_EVENTS = "poll_events";
    private static final String DEFAULT_PERIODIC_SYNC_INTERVAL = "60";
    private static final String DEFAULT_MAX_EVENTS = "500";
    private static final String DEFAULT_MAX_VMS = "500";
    private static final boolean DEFAULT_POLL_EVENTS = true;

    @Bean
    EventsHandler eventsHandler;

    Preference periodicSyncIntervalPref;
    Preference maxEventsPref;
    Preference maxVmsPref;
    SharedPreferences sharedPreferences;

    public static int getSyncIntervalInMinutes(SharedPreferences sharedPreferences) {
        return Integer.parseInt(sharedPreferences
                .getString(KEY_PERIODIC_SYNC_INTERVAL, DEFAULT_PERIODIC_SYNC_INTERVAL));
    }

    public static int getMaxEvents(SharedPreferences sharedPreferences) {
        return Integer.parseInt(sharedPreferences.getString(KEY_MAX_EVENTS, DEFAULT_MAX_EVENTS));
    }

    public static int getMaxVms(SharedPreferences sharedPreferences) {
        return Integer.parseInt(sharedPreferences.getString(KEY_MAX_VMS, DEFAULT_MAX_VMS));
    }

    public static boolean isPollEventsEnabled(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(KEY_POLL_EVENTS, DEFAULT_POLL_EVENTS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        addPreferencesFromResource(R.xml.preferences);

        Preference aboutButton = (Preference) findPreference("about_button");
        aboutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Dialog dialog = new Dialog(SettingsActivity.this);
                dialog.setContentView(R.layout.about_dialog);
                dialog.setTitle(getString(R.string.prefs_about_moVirt));
                dialog.show();
                return true;
            }
        });

        Preference button = (Preference) findPreference("connection_button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent intent = new Intent(
                        getApplicationContext(),
                        AuthenticatorActivity_.class);
                startActivity(intent);
                return true;
            }
        });

        periodicSyncIntervalPref = findPreference(KEY_PERIODIC_SYNC_INTERVAL);
        periodicSyncIntervalPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String errorMessage = "Interval should be not less then 1 minute.";
                int newValueInt;
                try {
                    newValueInt = Integer.parseInt((String) newValue);
                    if (newValueInt < 1) {
                        Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
        maxEventsPref = findPreference(KEY_MAX_EVENTS);
        maxEventsPref.setOnPreferenceChangeListener(new IntegerValidator());
        maxVmsPref = findPreference(KEY_MAX_VMS);
        maxVmsPref.setOnPreferenceChangeListener(new IntegerValidator());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(KEY_PERIODIC_SYNC, false)) {
            periodicSyncIntervalPref.setEnabled(true);
        } else {
            periodicSyncIntervalPref.setEnabled(false);
        }

        setSyncIntervalPrefSummary();
        setMaxVmsSummary();
        setMaxEventsSummary();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
            case KEY_PERIODIC_SYNC:
                if (sharedPreferences.getBoolean(key, false)) {
                    periodicSyncIntervalPref.setEnabled(true);
                    int intervalInMinutes = getSyncIntervalInMinutes(sharedPreferences);
                    AuthenticatorActivity.addPeriodicSync(intervalInMinutes);
                } else {
                    periodicSyncIntervalPref.setEnabled(false);
                    ContentResolver.removePeriodicSync(
                            MovirtAuthenticator.MOVIRT_ACCOUNT,
                            OVirtContract.CONTENT_AUTHORITY,
                            Bundle.EMPTY);
                }
                break;
            case KEY_PERIODIC_SYNC_INTERVAL:
                int intervalInMinutes = getSyncIntervalInMinutes(sharedPreferences);
                AuthenticatorActivity.addPeriodicSync(intervalInMinutes);
                setSyncIntervalPrefSummary();
                break;
            case KEY_MAX_EVENTS:
                setMaxEventsSummary();
                eventsHandler.setMaxEventsStored(getMaxEvents(sharedPreferences));
                break;
            case KEY_MAX_VMS:
                setMaxVmsSummary();
                break;
        }
    }

    private void setMaxEventsSummary() {
        String maxEvents = sharedPreferences.getString(KEY_MAX_EVENTS, DEFAULT_MAX_EVENTS);
        maxEventsPref.setSummary(getString(
                R.string.prefs_max_events_locally_summary, maxEvents));
    }

    private void setMaxVmsSummary() {
        String maxVms = sharedPreferences.getString(KEY_MAX_VMS, DEFAULT_MAX_VMS);
        maxVmsPref.setSummary(getString(
                R.string.prefs_max_vms_polled_summary, maxVms));
    }

    private void setSyncIntervalPrefSummary() {
        String interval = sharedPreferences
                .getString(KEY_PERIODIC_SYNC_INTERVAL, DEFAULT_PERIODIC_SYNC_INTERVAL);
        periodicSyncIntervalPref.setSummary(getString(
                R.string.prefs_periodic_sync_interval_summary, interval));
    }

    private class IntegerValidator implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            try {
                Integer.parseInt((String) newValue);
            } catch (NumberFormatException e) {
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }
    }
}
