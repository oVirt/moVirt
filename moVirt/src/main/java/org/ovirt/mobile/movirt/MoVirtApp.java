package org.ovirt.mobile.movirt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.ovirt.mobile.movirt.provider.OVirtAccountAuthenticator;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.service.IUpdaterService;

@EApplication
public class MoVirtApp extends Application {
    private IUpdaterService updaterService;

    public IUpdaterService getUpdaterService() {
        return updaterService;
    }

    @Pref
    AppPrefs_ prefs;

    public boolean endpointConfigured() {
        return prefs.endpoint().exists() &&
                prefs.username().exists() &&
                prefs.password().exists();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        final Account account = new Account(OVirtAccountAuthenticator.ACCOUNT_NAME, OVirtAccountAuthenticator.ACCOUNT_TYPE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Only change sync settings if it did not exist, yet
            //ContentResolver.setIsSyncable(account, MyAccountAuthenticator.CONTENT_AUTHORITY, 1);
            //ContentResolver.setSyncAutomatically(account, MyAccountAuthenticator.CONTENT_AUTHORITY, true);
            // Sync every 5 minutes by default
            ContentResolver.addPeriodicSync(account, OVirtContract.CONTENT_AUTHORITY, new Bundle(), 300);
        }
    }
}
