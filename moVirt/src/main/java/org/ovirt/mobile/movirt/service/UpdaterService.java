package org.ovirt.mobile.movirt.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.ovirt.mobile.movirt.rest.Cluster;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.rest.Vm;

import java.util.ArrayList;
import java.util.List;

@EService
public class UpdaterService extends Service implements IUpdaterService {

    private static final String TAG = UpdaterService.class.getSimpleName();
    public static final String VM_LIST_UPDATE = "org.ovirt.mobile.movirt.service.UpdaterService.VM_LIST_UPDATE";
    public static final String CLUSTER_LIST_UPDATE = "org.ovirt.mobile.movirt.service.UpdaterService.CLUSTER_LIST_UPDATE";
    private static final String EXTRA_CLUSTER_NAME = "cluster_name";
    private static final String EXTRA_VMS = "vms";
    private static final String EXTRA_CLUSTERS = "clusters";

    private ArrayList<Vm> vms = new ArrayList<>();
    private ArrayList<Cluster> clusters = new ArrayList<>();

    @Bean
    OVirtClient client;

    @Override
    public void fullUpdate() {
        Log.i(TAG, "full update triggered");

        updateAllVms();
        updateAllClusters();
    }

    @SuppressWarnings("ConstantConditions")
    @Background
    void updateAllVms() {
        vms.clear();
        vms.addAll(client.getVms());
        Intent intent = new Intent(VM_LIST_UPDATE);
        intent.getExtras().putString(EXTRA_CLUSTER_NAME, null);
        //intent.getExtras().putParcelableArrayList(EXTRA_VMS, vms);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @SuppressWarnings("ConstantConditions")
    @Background
    void updateAllClusters() {
        clusters.clear();
        clusters.addAll(client.getClusters());
        Intent intent = new Intent(CLUSTER_LIST_UPDATE);
        //intent.getExtras().putParcelableArrayList(EXTRA_CLUSTERS, clusters);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class Updater extends Binder {
        public IUpdaterService getService() {
            return UpdaterService.this;
        }
    }

    class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            fullUpdate();
        }
    }

    private final Updater binder = new Updater();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate called");
        fullUpdate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}