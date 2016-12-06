package org.ovirt.mobile.movirt.util;

import android.support.annotation.Nullable;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URIUtils {

    private static final String HTTP = "http://";
    private static final String ENGINE_360_CERTIFICATE_PATH = "/ovirt-engine/services/pki-resource?resource=ca-certificate&format=X509-PEM-CA";
    private static final String ENGINE_DEPRECATED_CERTIFICATE_PATH = "/ca.crt";

    @Nullable
    public static URL parseUrlSafe(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * @throws IllegalArgumentException with "URL is not valid" message if url not correct
     */
    public static URL tryToParseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid");
        }
    }

    @Nullable
    public static URI getURIWithoutParameters(URI uri) {
        try {
            if (uri == null) {
                return null;
            }

            return UriComponentsBuilder.newInstance().scheme(uri.getScheme())
                    .host(uri.getHost())
                    .port(uri.getPort())
                    .path(uri.getPath())
                    .build()
                    .toUri();
        } catch (Exception e) {
            return null;
        }
    }

    public static URL[] getEngineCertificateUrls(URL hostUrl) {
        String version360 = HTTP + hostUrl.getHost() + ENGINE_360_CERTIFICATE_PATH;
        String versionDeprecated = HTTP + hostUrl.getHost() + ENGINE_DEPRECATED_CERTIFICATE_PATH;

        return new URL[]{
                parseUrlSafe(version360),
                parseUrlSafe(versionDeprecated)};
    }
}
