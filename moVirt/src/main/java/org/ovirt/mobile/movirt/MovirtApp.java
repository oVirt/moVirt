package org.ovirt.mobile.movirt;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EApplication;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import org.ovirt.mobile.movirt.rest.AuthInterceptor;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;


@EApplication
public class MovirtApp extends Application {

    @Bean
    AuthInterceptor authInterceptor;

    @RestService
    OVirtClient client;

    public static final String ROOT_URL = "http://10.0.2.2:8080/api";

    @Pref
    AppPrefs_ prefs;

    @AfterInject
    void initClient() {
        RestTemplate template = client.getRestTemplate();
        template.setInterceptors(Arrays.asList((ClientHttpRequestInterceptor) authInterceptor));
        updateRootUrlFromSettings();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("endpoint")) {
                    updateRootUrlFromSettings();
                }
            }
        });
    }

    private void updateRootUrlFromSettings() {
        client.setRootUrl(prefs.endpoint().get());
    }

    public OVirtClient getClient() {
        return client;
    }
}
