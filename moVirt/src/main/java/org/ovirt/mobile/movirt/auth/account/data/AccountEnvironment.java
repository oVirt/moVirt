package org.ovirt.mobile.movirt.auth.account.data;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper_;

public class AccountEnvironment {

    private SharedPreferencesHelper sharedPreferencesHelper;

    public AccountEnvironment(Account account, Context context) {
        sharedPreferencesHelper = SharedPreferencesHelper_.getInstance_(context);
        sharedPreferencesHelper.initialize(account);
    }

    public void destroy() {
        sharedPreferencesHelper.destroy();
    }

    @NonNull
    public SharedPreferencesHelper getSharedPreferencesHelper() {
        return sharedPreferencesHelper;
    }
}
