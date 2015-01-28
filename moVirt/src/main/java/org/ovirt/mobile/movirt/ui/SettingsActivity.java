package org.ovirt.mobile.movirt.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@EActivity
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference aboutButton = (Preference)findPreference("about_button");
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

        Preference button = (Preference)findPreference("connection_button");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent intent = new Intent(getApplicationContext(), AuthenticatorActivity_.class);
                startActivity(intent);
                return true;
            }
        });

    }
}
