package org.ovirt.mobile.movirt.rest;

import android.util.Log;
import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by sphoorti on 19/11/14.
 */
@EBean
public class OvirtSimpleClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    @Bean
    NullHostnameVerifier verifier;
    private static final String TAG = OvirtSimpleClientHttpRequestFactory.class.getSimpleName();
    private boolean ignoreHttps;
    private SSLSocketFactory properSocketFactory;

    @AfterInject
    void initFactory() {
        properSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    public void setIgnoreHttps(boolean ignoreHttps) {
        this.ignoreHttps = ignoreHttps;
        if (ignoreHttps) {
            trustAllHosts();
        } else {
            untrustHosts();
        }
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        Log.d(TAG, "Prepare Connection");
        if (connection instanceof HttpsURLConnection && ignoreHttps) {
            Log.d(TAG, "Inside Prepare Connection");
            ((HttpsURLConnection) connection).setHostnameVerifier(verifier);
        }
        super.prepareConnection(connection, httpMethod);
    }

    /**
     * Trust every server - dont check for any certificate
     */
    private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }
    };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method enables certificate checking.
     */
    private void untrustHosts() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(properSocketFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}