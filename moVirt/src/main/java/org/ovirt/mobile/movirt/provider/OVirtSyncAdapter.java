package org.ovirt.mobile.movirt.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.rest.Cluster;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.rest.Vm;

import java.util.List;

@EBean
public class OVirtSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = OVirtSyncAdapter.class.getSimpleName();

    @Bean
    OVirtClient client;

    public OVirtSyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final List<Vm> vms = client.getVms();
        final List<Cluster> clusters = client.getClusters();

        for (Vm vm : vms) {
            Log.i(TAG, "Fetched: " + vm.toString());
        }
        for (Cluster cluster : clusters) {
            Log.i(TAG, "Fetched: " + cluster.toString());
        }
    }
}
