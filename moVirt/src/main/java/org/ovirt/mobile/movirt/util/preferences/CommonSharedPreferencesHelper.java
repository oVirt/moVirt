package org.ovirt.mobile.movirt.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringRes;

@EBean(scope = EBean.Scope.Singleton)
public class CommonSharedPreferencesHelper {

    @RootContext
    Context context;

    @StringRes
    String defaultActiveAccountName;

    private SharedPreferences sharedPreferences;

    @AfterInject
    void initialize() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getActiveAccountName() {
        return sharedPreferences.getString(SettingsKey.ACTIVE_ACCOUNT_NAME.getValue(), defaultActiveAccountName);
    }

    /**
     * @param activeAccountName name of active account
     * @return Returns true if the new values were successfully written to persistent storage.
     */
    public boolean setActiveAccountName(String activeAccountName) {
        return sharedPreferences.edit()
                .putString(SettingsKey.ACTIVE_ACCOUNT_NAME.getValue(), activeAccountName)
                .commit();
    }
}
