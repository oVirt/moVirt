package org.ovirt.mobile.movirt.provider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean
public class SyncService extends Service {

    @Bean
    OVirtSyncAdapter syncAdapter;

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
