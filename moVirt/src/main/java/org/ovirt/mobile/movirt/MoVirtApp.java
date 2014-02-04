package org.ovirt.mobile.movirt;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.androidannotations.annotations.EApplication;
import org.ovirt.mobile.movirt.service.IUpdaterService;
import org.ovirt.mobile.movirt.service.UpdaterService;
import org.ovirt.mobile.movirt.service.UpdaterService_;

@EApplication
public class MoVirtApp extends Application implements ServiceConnection {
    private IUpdaterService updaterService;

    public IUpdaterService getUpdaterService() {
        return updaterService;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, UpdaterService_.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (updaterService != null) {
            unbindService(this);
            updaterService = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        UpdaterService.Updater binder = (UpdaterService.Updater) service;
        updaterService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        updaterService = null;
    }
}
