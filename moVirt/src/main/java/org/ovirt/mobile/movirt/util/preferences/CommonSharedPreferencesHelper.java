package org.ovirt.mobile.movirt.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.BooleanRes;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.util.JsonUtils;

@EBean(scope = EBean.Scope.Singleton)
public class CommonSharedPreferencesHelper {

    @RootContext
    Context context;

    @BooleanRes
    boolean defaultFirstAccountConfigured;

    @BooleanRes
    boolean defaultPasswordVisibility;

    private SharedPreferences sharedPreferences;

    @AfterInject
    void initialize() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public StoredActiveSelection getActiveSelection() {
        String active = sharedPreferences.getString(SettingsKey.ACTIVE_SELECTION.getValue(), null);
        try {
            return JsonUtils.stringToObject(active, StoredActiveSelection.class);
        } catch (Exception x) {
            return new StoredActiveSelection();
        }
    }

    public boolean setActiveSelection(StoredActiveSelection activeSelection) {
        return sharedPreferences.edit()
                .putString(SettingsKey.ACTIVE_SELECTION.getValue(), JsonUtils.objectToString(activeSelection))
                .commit();
    }

    public boolean isFirstAccountConfigured() {
        return sharedPreferences.getBoolean(SettingsKey.FIRST_ACCOUNT_CONFIGURED.getValue(), defaultFirstAccountConfigured);
    }

    public boolean setFirstAccountConfigured(boolean firstAccountConfigured) {
        return sharedPreferences.edit()
                .putBoolean(SettingsKey.FIRST_ACCOUNT_CONFIGURED.getValue(), firstAccountConfigured)
                .commit();
    }

    public boolean isPasswordVisible() {
        return sharedPreferences.getBoolean(SettingsKey.PASSWORD_VISIBILITY.getValue(), defaultPasswordVisibility);
    }

    public boolean setPasswordVisibility(boolean visible) {
        return sharedPreferences.edit()
                .putBoolean(SettingsKey.PASSWORD_VISIBILITY.getValue(), visible)
                .commit();
    }

    public static class StoredActiveSelection {
        public String accountId;
        public String clusterId;

        public StoredActiveSelection() {
        }

        public StoredActiveSelection(ActiveSelection activeSelection) {
            this.accountId = activeSelection.getAccountId();
            this.clusterId = activeSelection.getClusterId();
        }
    }
}
