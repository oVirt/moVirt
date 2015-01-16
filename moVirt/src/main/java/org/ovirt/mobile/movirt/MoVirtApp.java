package org.ovirt.mobile.movirt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.SyncUtils;

@EApplication
public class MoVirtApp extends Application {
    private static final String TAG = MoVirtApp.class.getSimpleName();

    public static final String CONNECTION_FAILURE = "org.ovirt.mobile.movirt.CONNECTION_FAILURE";

    public static final String REST_REQUEST_FAILED = "org.ovirt.mobile.movirt.REST_REQUEST_FAILED";

    public static final String IN_SYNC = "org.ovirt.mobile.movirt.IN_SYNC";

    public static final String EVENTS_IN_SYNC = "org.ovirt.mobile.movirt.EVENTS_IN_SYNC";

    public static final String SYNCING = "org.ovirt.mobile.movirt.SYNCING";

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        SyncUtils.createSyncAccount(this);
    }

    public boolean endpointConfigured() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("endpoint", null) != null &&
                preferences.getString("username", null) != null &&
                preferences.getString("password", null) != null;
    }
}
