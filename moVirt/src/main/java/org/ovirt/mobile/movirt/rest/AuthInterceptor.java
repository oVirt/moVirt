package org.ovirt.mobile.movirt.rest;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.api.Scope;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@EBean(scope = Scope.Singleton)
public class AuthInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution)
            throws IOException {
        HttpHeaders headers = httpRequest.getHeaders();
        HttpAuthentication auth = new HttpBasicAuthentication("admin@internal", "engine");
        headers.setAuthorization(auth);
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }
}