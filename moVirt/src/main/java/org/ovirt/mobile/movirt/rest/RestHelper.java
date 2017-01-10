package org.ovirt.mobile.movirt.rest;

import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
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

    public static <T extends RestClientHeaders & RestClientSupport> void setAcceptEncodingHeaderAndFactory(T restClient, ClientHttpRequestFactory requestFactory) {
        restClient.setHeader(ACCEPT_ENCODING, "gzip");
        restClient.getRestTemplate().setRequestFactory(requestFactory);
    }

    public static <T extends RestClientHeaders> void setVersionHeader(T restClient, Version version) {
        setVersionHeader(restClient, version.getMajor());
    }

    public static <T extends RestClientHeaders> void setVersionHeader(T restClient, int version) {
        setVersionHeader(restClient, String.valueOf(version));
    }

    private static <T extends RestClientHeaders> void setVersionHeader(T restClient, String version) {
        restClient.setHeader(VERSION, version);
    }

    public static <T extends RestClientHeaders> void setAcceptHeader(T restClient, String accept) {
        restClient.setHeader(ACCEPT, accept);
    }

    public static <T extends RestClientHeaders> void setFilterHeader(T restClient, boolean hasAdminPermissions) {
        restClient.setHeader(FILTER, Boolean.toString(!hasAdminPermissions));
    }

    public static <T extends RestClientHeaders> void setPersistentV3AuthHeaders(T restClient) {
        restClient.setHeader(SESSION_TTL, "120"); // 2h
        restClient.setHeader(PREFER, "persistent-auth, csrf-protection");
    }

    public static <T extends RestClientHeaders> void setupAuth(T restClient, Version version) {
        if (version.isV3Api()) {
            clearV4Auth(restClient);
            setPersistentV3AuthHeaders(restClient);
        } else {
            clearV3Auth(restClient);
        }
    }

    public static <T extends RestClientHeaders> void prepareAuthToken(T restClient, Version version, String token) {
        if (version.isV3Api()) {
            restClient.setCookie(JSESSIONID, token);
        } else {
            restClient.setBearerAuth(token);
        }
    }

    public static <T extends RestClientHeaders> void clearAuth(T restClient) {
        clearV3Auth(restClient);
        clearV4Auth(restClient);
    }

    private static <T extends RestClientHeaders> void clearV3Auth(T restClient) {
        restClient.setHeader(SESSION_TTL, "");
        restClient.setHeader(PREFER, "");
        restClient.setCookie(JSESSIONID, "");
    }

    private static <T extends RestClientHeaders> void clearV4Auth(T restClient) {
        restClient.setAuthentication(new HttpAuthentication() {
            @Override
            public String getHeaderValue() {
                // empty authentication - e.g. not the basic one
                return "";
            }
        });
    }
}
