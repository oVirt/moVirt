package org.ovirt.mobile.movirt.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.PreferenceByKey;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.AccountManagerHelper;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.provider.EventProviderHelper;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.ConnectionSettingsActivity_;
import org.ovirt.mobile.movirt.util.Disposables;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SettingsKey;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EActivity
public class SettingsActivity extends BroadcastAwareAppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MovirtAccount account = getIntent().getParcelableExtra(Constants.ACCOUNT_KEY);
        setTitle(getString(R.string.title_activity_account_settings, account.getName()));
        getFragmentManager().beginTransaction().replace(android.R.id.content, SettingsFragment.newInstance(account)).commit();
    }

    @EFragment
    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final String ACCOUNT_NAME_KEY = "ACCOUNT_NAME_KEY";
        private static final int OBJECTS_SAVE_LEVEL_THRESHOLD = 5000;

        @PreferenceByKey(R.string.connection_button_pref_key)
        Preference connectionButton;

        @PreferenceByKey(R.string.poll_events_pref_key)
        Preference pollEvents;

        @PreferenceByKey(R.string.connection_notification_pref_key)
        Preference connectionNotification;

        @PreferenceByKey(R.string.periodic_sync_pref_key)
        CheckBoxPreference periodicSync;

        @PreferenceByKey(R.string.periodic_sync_interval_pref_key)
        Preference periodicSyncInterval;

        @PreferenceByKey(R.string.max_events_polled_pref_key)
        Preference maxEventsPolled;

        @PreferenceByKey(R.string.max_events_stored_pref_key)
        Preference maxEventsStored;

        @PreferenceByKey(R.string.events_search_query_pref_key)
        Preference eventsSearchQuery;

        @PreferenceByKey(R.string.max_vms_polled_pref_key)
        Preference maxVmsPolled;

        @PreferenceByKey(R.string.vms_search_query_pref_key)
        Preference vmsSearchQuery;

        @Bean
        EnvironmentStore environmentStore;

        @Bean
        CommonMessageHelper messageHelper;

        @Bean
        AccountRxStore rxStore;

        @Bean
        AccountManagerHelper accountManagerHelper;

        private SharedPreferencesHelper sharedPreferencesHelper;
        private EventProviderHelper eventProviderHelper;

        @InstanceState
        MovirtAccount account;

        private Disposables disposables = new Disposables();

        public static SettingsFragment newInstance(MovirtAccount account) {
            SettingsFragment fragment = new SettingsActivity_.SettingsFragment_();
            Bundle args = new Bundle();
            args.putParcelable(ACCOUNT_NAME_KEY, account);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            account = getArguments().getParcelable(ACCOUNT_NAME_KEY);

            ObjectUtils.requireNotNull(account, "account");

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(account.getId() + Constants.PREFERENCES_NAME_SUFFIX);
            prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
            toggleVisibility();

            addPreferencesFromResource(R.xml.preferences);
            disposables.add(rxStore.onRemovedAccountObservable(account)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(account -> getActivity().finish()));

            try {
                sharedPreferencesHelper = environmentStore.getSharedPreferencesHelper(account);
                eventProviderHelper = environmentStore.getEventProviderHelper(account);
            } catch (AccountDeletedException e) {
                getActivity().finish();
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            disposables.destroy();
        }

        public void toggleVisibility() {
            boolean enabled = account != null;

            if (pollEvents != null && vmsSearchQuery != null) { // also the others
                pollEvents.setEnabled(enabled);
                connectionNotification.setEnabled(enabled);
                periodicSync.setEnabled(enabled);
                maxEventsPolled.setEnabled(enabled);
                maxEventsStored.setEnabled(enabled);
                eventsSearchQuery.setEnabled(enabled);
                maxVmsPolled.setEnabled(enabled);
                vmsSearchQuery.setEnabled(enabled);
            }
        }

        @AfterViews
        public void afterViews() {
            if (getActivity().isFinishing()) {
                return; // account deleted: activity is finishing
            }

            connectionButton.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(
                        getActivity().getApplicationContext(),
                        ConnectionSettingsActivity_.class);
                intent.putExtra(Constants.ACCOUNT_KEY, account);
                startActivity(intent);
                return true;
            });

            periodicSyncInterval.setOnPreferenceChangeListener((preference, newValue) -> {
                int newValueInt;
                try {
                    newValueInt = Integer.parseInt((String) newValue);
                    if (newValueInt < 1) {
                        messageHelper.showToast("Interval should not be shorter than 1 minute.");
                        return false;
                    }
                    if (newValueInt <= 10) {
                        messageHelper.showToast("The syncs may be delayed considerably by Android (mainly 7+)");
                    }
                } catch (NumberFormatException e) {
                    messageHelper.showToast(e.getMessage());
                    return false;
                }

                return true;
            });

            maxEventsPolled.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int polled = Integer.parseInt((String) newValue);
                    informAboutMaxValues(polled);
                    return checkEvents(polled, sharedPreferencesHelper.getMaxEventsStored());
                } catch (NumberFormatException e) {
                    messageHelper.showToast(e.getMessage());
                    return false;
                }
            });

            maxEventsStored.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int stored = Integer.parseInt((String) newValue);
                    return checkEvents(sharedPreferencesHelper.getMaxEventsPolled(), stored);
                } catch (NumberFormatException e) {
                    messageHelper.showToast(e.getMessage());
                    return false;
                }
            });

            maxVmsPolled.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int value = Integer.parseInt((String) newValue);
                    informAboutMaxValues(value);
                } catch (NumberFormatException e) {
                    messageHelper.showToast(e.getMessage());
                    return false;
                }
                return true;
            });

            setSyncIntervalPrefSummary();
            setMaxVmsSummary();
            setMaxEventsPolledSummary();
            setMaxEventsStoredSummary();
            toggleVisibility();
        }

        @Override
        public void onResume() {
            super.onResume();
            checkPeriodicSyncStatusState();

            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        private void checkPeriodicSyncStatusState() {
            boolean periodicSyncable = accountManagerHelper.isPeriodicSyncable(account);
            // refresh preferences if changed since last time (e.g. system settings)
            final SharedPreferencesHelper preferencesHelper = environmentStore.getSharedPreferencesHelper(account);
            if (periodicSyncable != preferencesHelper.isPeriodicSyncEnabled()) {
                sharedPreferencesHelper.setPeriodicSync(periodicSyncable);
            }

            periodicSyncInterval.setEnabled(account != null && periodicSyncable);
            periodicSync.setChecked(periodicSyncable);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String prefKey) {
            SettingsKey key = SettingsKey.from(prefKey);
            switch (key) {
                case PERIODIC_SYNC:
                    final Boolean periodicSyncable = sharedPreferencesHelper.getBooleanPref(key);
                    periodicSyncInterval.setEnabled(account != null && periodicSyncable);
                    accountManagerHelper.updatePeriodicSync(account, periodicSyncable);
                    break;
                case PERIODIC_SYNC_INTERVAL:
                    accountManagerHelper.updatePeriodicSync(account, accountManagerHelper.isPeriodicSyncable(account));
                    setSyncIntervalPrefSummary();
                    break;
                case MAX_EVENTS_POLLED:
                    setMaxEventsPolledSummary();
                    break;
                case MAX_EVENTS_STORED:
                    setMaxEventsStoredSummary();
                    eventProviderHelper.deleteTemporaryEvents();
                    eventProviderHelper.deleteOldEvents();
                    break;
                case MAX_VMS:
                    setMaxVmsSummary();
                    break;
            }
        }

        private void setMaxEventsPolledSummary() {
            int maxEvents = sharedPreferencesHelper.getMaxEventsPolled();
            maxEventsPolled.setSummary(getString(
                    R.string.prefs_max_events_polled_summary, maxEvents));
        }

        private void setMaxEventsStoredSummary() {
            int maxEvents = sharedPreferencesHelper.getMaxEventsStored();
            maxEventsStored.setSummary(getString(
                    R.string.prefs_max_events_stored_summary, maxEvents));
        }

        private void setMaxVmsSummary() {
            int maxVms = sharedPreferencesHelper.getMaxVms();
            maxVmsPolled.setSummary(getString(
                    R.string.prefs_max_vms_polled_summary, maxVms));
        }

        private void setSyncIntervalPrefSummary() {
            int interval = sharedPreferencesHelper.getPeriodicSyncInterval();
            periodicSyncInterval.setSummary(getString(
                    R.string.prefs_periodic_sync_interval_summary, interval));
        }

        private void informAboutMaxValues(int objectsLimit) {
            if (objectsLimit > OBJECTS_SAVE_LEVEL_THRESHOLD) {
                messageHelper.showToast(getString(R.string.objects_save_level_threshold_message));
            }
        }

        private boolean checkEvents(int eventsPolled, int eventsStored) {
            if (eventsStored < eventsPolled) {
                messageHelper.showToast("events polled shouldn't be larger than events stored");
                return false;
            }
            return true;
        }
    }
}
