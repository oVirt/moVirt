package org.ovirt.mobile.movirt;

import android.app.Application;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EApplication;
import com.googlecode.androidannotations.annotations.rest.RestService;

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

    @AfterInject
    void initClient() {
        RestTemplate template = client.getRestTemplate();
        template.setInterceptors(Arrays.asList((ClientHttpRequestInterceptor) authInterceptor));
        client.setRootUrl(ROOT_URL);
    }

    public OVirtClient getClient() {
        return client;
    }
}
