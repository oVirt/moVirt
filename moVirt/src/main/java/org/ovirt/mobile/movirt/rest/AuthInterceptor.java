package org.ovirt.mobile.movirt.rest;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import com.googlecode.androidannotations.api.Scope;

import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import org.ovirt.mobile.movirt.*;

import java.io.IOException;

@EBean(scope = Scope.Singleton)
public class AuthInterceptor implements ClientHttpRequestInterceptor {
    @Pref
    AppPrefs_ prefs;

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution)
            throws IOException {
        HttpHeaders headers = httpRequest.getHeaders();
        final String username = prefs.username().get();
        final String password = prefs.password().get();
        headers.setAuthorization(new HttpBasicAuthentication(username, password));
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }
}