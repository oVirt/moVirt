package org.ovirt.mobile.movirt.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.account.EditAccountsActivity_;

@EActivity
public class MainSettingsActivity extends BroadcastAwareAppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettingsActivity_.MainSettingsFragment_()).commit();
    }

    @EFragment
    public static class MainSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @PreferenceByKey(R.string.engines_settings_pref_key)
        Preference enginesSettings;

        @PreferenceByKey(R.string.global_sync_pref_key)
        Preference globalSync;

        @PreferenceByKey(R.string.users_guide_pref_key)
        Preference usersGuide;

        @PreferenceByKey(R.string.about_button_pref_key)
        Preference aboutButton;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.main_preferences);
        }

        @AfterViews
        public void afterViews() {

            enginesSettings.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(
                        getActivity().getApplicationContext(),
                        EditAccountsActivity_.class);
                startActivity(intent);
                return true;
            });

            usersGuide.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.moVirt_users_guide_link)));
                startActivity(browserIntent);
                return true;
            });

            aboutButton.setOnPreferenceClickListener(preference -> {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.about_dialog);
                dialog.setTitle(getString(R.string.prefs_about_moVirt));
                ((TextView) dialog.findViewById(R.id.app_readme)).setMovementMethod(LinkMovementMethod.getInstance());
                dialog.show();
                return true;
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String prefKey) {
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
