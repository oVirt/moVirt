package org.ovirt.mobile.movirt;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import org.ovirt.mobile.movirt.rest.Vm;

import java.util.ArrayList;
import java.util.List;

public class UpdaterService extends IntentService {

    private static final String TAG = UpdaterService.class.getSimpleName();

    private List<Vm> vms = new ArrayList<>();

    public UpdaterService() {
        super("org.ovirt.mobile.ovirt.UpdaterService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
