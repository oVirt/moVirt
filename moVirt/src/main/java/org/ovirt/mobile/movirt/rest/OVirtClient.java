package org.ovirt.mobile.movirt.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.ovirt.mobile.movirt.AppPrefs_;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.provider.OVirtContract;

import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class OVirtClient implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = OVirtClient.class.getSimpleName();
    @RestService
    OVirtRestClient restClient;

    public List<Vm> getVms() {
        Log.d(TAG, "Getting VMs using " + prefs.username().get() + " and " + prefs.password().get());
        return restClient.getVms().vm;
    }

    public List<Vm> getVmsByClusterName(String clusterName) {
        return restClient.getVms("cluster=" + clusterName).vm;
    }

    public List<Cluster> getClusters() {
        return restClient.getClusters().cluster;
    }

    @Pref
    AppPrefs_ prefs;

    @App
    MoVirtApp app;

    @AfterInject
    void initClient() {
        updateRootUrlFromSettings();
        updateAuthenticationFromSettings();
        registerSharedPreferencesListener();
    }

    private void updateRootUrlFromSettings() {
        restClient.setRootUrl(prefs.endpoint().get());
    }

    private void updateAuthenticationFromSettings() {
        restClient.setHttpBasicAuth(prefs.username().get(), prefs.password().get());
    }

    private void registerSharedPreferencesListener() {
        app.getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("endpoint")) {
            updateRootUrlFromSettings();
        }
        if (key.equals("username") || key.equals("password")) {
            updateAuthenticationFromSettings();
        }
    }
}
