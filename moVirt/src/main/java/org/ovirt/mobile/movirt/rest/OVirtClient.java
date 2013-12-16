package org.ovirt.mobile.movirt.rest;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.ovirt.mobile.movirt.AppPrefs_;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class OVirtClient {
    @RestService
    OVirtRestClient restClient;

    public List<Vm> getVms() {
        return restClient.getVms().vms;
    }

    public List<Vm> getVmsByClusterName(String clusterName) {
        return restClient.getVms("cluster=" + clusterName).vms;
    }

    public List<Cluster> getClusters() {
        return restClient.getClusters().clusters;
    }

    @Bean
    AuthInterceptor authInterceptor;

    public static final String ROOT_URL = "http://10.0.2.2:8080/ovirt-engine/api";

    @Pref
    AppPrefs_ prefs;

    @App
    MoVirtApp app;

    @AfterInject
    void initClient() {
        setAuthInterceptor();
        updateRootUrlFromSettings();
        registerSharedPreferencesListener();
    }

    private void updateRootUrlFromSettings() {
        restClient.setRootUrl(prefs.endpoint().get());
    }

    private void setAuthInterceptor() {
        RestTemplate template = restClient.getRestTemplate();
        template.setInterceptors(Arrays.asList((ClientHttpRequestInterceptor) authInterceptor));
    }

    private void registerSharedPreferencesListener() {
        PreferenceManager.getDefaultSharedPreferences(app)
                .registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if (key.equals("endpoint")) {
                            updateRootUrlFromSettings();
                        }
                    }
                });
    }
}
