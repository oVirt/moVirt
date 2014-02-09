package org.ovirt.mobile.movirt.sync;

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
import org.ovirt.mobile.movirt.auth.AuthenticatorService;
import org.ovirt.mobile.movirt.rest.Cluster;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.rest.Vm;

import java.util.List;

@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    @Bean
    OVirtClient client;

    final AccountManager accountManager;

    public SyncAdapter(Context context) {
        super(context, true);
        accountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Performing full sync for account[" + account.name + "]");

        try {
//            String authToken = accountManager.blockingGetAuthToken(account, AuthenticatorService.AccountAuthenticator.DEFAULT_AUTH_TOKEN_TYPE, true);
//            Log.i(TAG, "Auth token: " + authToken);
            final List<Vm> vms = client.getVms();
            final List<Cluster> clusters = client.getClusters();

            for (Vm vm : vms) {
                Log.i(TAG, "Fetched: " + vm.toString());
            }
            for (Cluster cluster : clusters) {
                Log.i(TAG, "Fetched: " + cluster.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
