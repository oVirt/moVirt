package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.EBean;

import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

@EBean
public class ListHostnameVerifier implements HostnameVerifier {

    private List<String> trustedHosts;

    public boolean verify(String hostname, SSLSession session) {
        if (trustedHosts == null || hostname == null) {
            return false;
        }

        return trustedHosts.contains(hostname.trim());
    }

    public void initToTrustedHosts(List<String> trustedHosts) {
        this.trustedHosts = trustedHosts;
    }
}
