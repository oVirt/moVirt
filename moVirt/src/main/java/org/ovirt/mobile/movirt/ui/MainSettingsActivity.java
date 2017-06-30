package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.AccountManagerHelper;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.account.EditAccountsActivity_;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;

@EActivity
public class MainSettingsActivity extends BroadcastAwareAppCompatActivity implements ConfirmDialogFragment.ConfirmDialogListener {

    private static final String USER_GUIDE_DIALOG_TAG = "userGuide";
    private static final int USER_GUIDE_DIALOG = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettingsActivity_.MainSettingsFragment_()).commit();
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (dialogButton == DialogInterface.BUTTON_POSITIVE && actionId == USER_GUIDE_DIALOG) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.moVirt_users_guide_link)));
            startActivity(browserIntent);
        }
    }

    public void showUsersGuideDialog() {
        DialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(USER_GUIDE_DIALOG, getString(R.string.dialog_action_user_guide), true);
        confirmDialog.show(getFragmentManager(), USER_GUIDE_DIALOG_TAG);
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

        @Bean
        AccountRxStore rxStore;

        @Bean
        AccountManagerHelper accountManagerHelper;

        @Bean
        CommonMessageHelper commonMessageHelper;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.main_preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String prefKey) {
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

            globalSync.setOnPreferenceClickListener(preference -> {
                for (MovirtAccount account : rxStore.getAllAccounts()) {
                    accountManagerHelper.updatePeriodicSync(account, false);
                }
                commonMessageHelper.showToast(getString(R.string.removed_syncs));
                return true;
            });

            usersGuide.setOnPreferenceClickListener(preference -> {
                Activity activity = getActivity();
                if (activity != null && activity instanceof MainSettingsActivity) {
                    ((MainSettingsActivity) activity).showUsersGuideDialog();
                }
                return true;
            });

            aboutButton.setOnPreferenceClickListener(preference -> {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.about_dialog);
                dialog.setTitle(getString(R.string.prefs_about_moVirt));
                ((TextView) dialog.findViewById(R.id.app_readme)).setMovementMethod(LinkMovementMethod.getInstance());
                ((TextView) dialog.findViewById(R.id.app_privacy_policy)).setMovementMethod(LinkMovementMethod.getInstance());
                dialog.show();
                return true;
            });
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
