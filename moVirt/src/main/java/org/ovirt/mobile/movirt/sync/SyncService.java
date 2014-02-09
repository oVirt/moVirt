package org.ovirt.mobile.movirt.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

@EService
public class SyncService extends Service {

    @Bean
    SyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("SyncService", "Creating sync service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
