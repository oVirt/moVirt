package org.ovirt.mobile.movirt;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.ovirt.mobile.movirt.sync.SyncUtils;

@EApplication
public class MoVirtApp extends Application {
    private static final String TAG = MoVirtApp.class.getSimpleName();

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Bean
    SyncUtils syncUtils;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        syncUtils.createSyncAccount();
    }

    public boolean endpointConfigured() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("endpoint", null) != null &&
                preferences.getString("username", null) != null &&
                preferences.getString("password", null) != null;
    }
}
