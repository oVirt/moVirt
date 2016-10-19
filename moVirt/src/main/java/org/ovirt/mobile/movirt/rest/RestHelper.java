package org.ovirt.mobile.movirt.rest;

import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;
import org.ovirt.mobile.movirt.util.Version;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.client.ClientHttpRequestFactory;

public class RestHelper {
    private static final String TAG = RestHelper.class.getSimpleName();

    public static final String JSESSIONID = "JSESSIONID";
    public static final String FILTER = "Filter";
    public static final String PREFER = "Prefer";
    public static final String SESSION_TTL = "Session-TTL";
    public static final String VERSION = "Version";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ACCEPT = "Accept";

    public static <T extends RestClientHeaders & RestClientSupport> void initClient(T restClient, ClientHttpRequestFactory requestFactory) {
        restClient.setHeader(ACCEPT_ENCODING, "gzip");
        restClient.getRestTemplate().setRequestFactory(requestFactory);
    }

    public static <T extends RestClientHeaders> void setupVersionHeader(T restClient, Version version) {
        setupVersionHeader(restClient, version.getMajor());
    }

    public static <T extends RestClientHeaders> void setupVersionHeader(T restClient, int version) {
        setupVersionHeader(restClient, String.valueOf(version));
    }

    private static <T extends RestClientHeaders> void setupVersionHeader(T restClient, String version) {
        restClient.setHeader(VERSION, version);
    }

    public static <T extends RestClientHeaders> void setAcceptHeader(T restClient, String accept) {
        restClient.setHeader(ACCEPT, accept);
    }

    public static <T extends RestClientHeaders> void setPersistentV3AuthHeaders(T restClient) {
        restClient.setHeader(SESSION_TTL, "120"); // 2h
        restClient.setHeader(PREFER, "persistent-auth, csrf-protection");
    }

    public static <T extends RestClientHeaders> void resetClientSettings(T restClient) {
        restClient.setHeader(SESSION_TTL, "");
        restClient.setHeader(PREFER, "");
        restClient.setCookie(JSESSIONID, "");
        restClient.setAuthentication(new HttpAuthentication() {
            @Override
            public String getHeaderValue() {
                // empty authentication - e.g. not the basic one
                return "";
            }
        });
    }

    public static <T extends RestClientRootUrl & RestClientHeaders & RestClientSupport> void updateClientBeforeCall(T restClient,
                                                                                                                    MovirtAuthenticator movirtAuthenticator) {
        updateClientBeforeCall(restClient, movirtAuthenticator.getApiUrl(), movirtAuthenticator);
    }

    public static <T extends RestClientRootUrl & RestClientHeaders & RestClientSupport> void updateClientBeforeCall(T restClient,
                                                                                                                    String rootUrl,
                                                                                                                    MovirtAuthenticator movirtAuthenticator) {
        updateClientBeforeCall(restClient, rootUrl, movirtAuthenticator.hasAdminPermissions(), movirtAuthenticator.getCertHandlingStrategy());
    }

    public static <T extends RestClientRootUrl & RestClientHeaders & RestClientSupport> void updateClientBeforeCall(T restClient,
                                                                                                                    String rootUrl,
                                                                                                                    boolean hasAdminPermissions,
                                                                                                                    CertHandlingStrategy certHandlingStrategy) {

        final ClientHttpRequestFactory requestFactory = restClient.getRestTemplate().getRequestFactory();
        if (!(requestFactory instanceof OvirtSimpleClientHttpRequestFactory)) {
            throw new IllegalArgumentException("Factory should support certificates");
        }
        OvirtSimpleClientHttpRequestFactory ovirtFactory = (OvirtSimpleClientHttpRequestFactory) restClient.getRestTemplate().getRequestFactory();
        ovirtFactory.setCertificateHandlingMode(certHandlingStrategy);
        restClient.setHeader(FILTER, Boolean.toString(!hasAdminPermissions));
        restClient.setRootUrl(rootUrl);
    }

}
