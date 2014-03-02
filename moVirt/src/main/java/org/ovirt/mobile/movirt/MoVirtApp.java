package org.ovirt.mobile.movirt;

import android.app.Application;

import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.DatabaseHelper;
import org.ovirt.mobile.movirt.sync.SyncUtils;

@EApplication
public class MoVirtApp extends Application {
    private static final String TAG = MoVirtApp.class.getSimpleName();

    @Pref
    AppPrefs_ prefs;

    @Override
    public void onCreate() {
        super.onCreate();

        SyncUtils.createSyncAccount(this);
    }

    public boolean endpointConfigured() {
        return prefs.endpoint().exists();// &&
//                prefs.username().exists() &&
//                prefs.password().exists();
    }
}
