package org.ovirt.mobile.movirt.rest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.model.CaCert;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@EBean(scope = EBean.Scope.Singleton)
public class OvirtSimpleClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private static final String TAG = OvirtSimpleClientHttpRequestFactory.class.getSimpleName();

    @Bean
    NullHostnameVerifier nullHostnameVerifier;

    @Bean
    ListHostnameVerifier listHostnameVerifier;

    @RootContext
    Context rootContext;

    @Bean
    ProviderFacade providerFacade;

    private SSLSocketFactory properSocketFactory;

    private CertHandlingStrategy certificateHandlingStrategy;

    @AfterInject
    void initFactory() {
        properSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public void setCertificateHandlingStrategy(CertHandlingStrategy certificateHandlingStrategy) {
        this.certificateHandlingStrategy = certificateHandlingStrategy;

        switch (certificateHandlingStrategy) {
            case TRUST_SYSTEM:
                trustOnlyKnownCerts();
                break;
            case TRUST_CUSTOM:
                trustImportedCert();
                break;
            case TRUST_ALL:
                trustAllHosts();
                break;
        }
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        Log.d(TAG, "Prepare Connection");
        if (connection instanceof HttpsURLConnection) {
            if (certificateHandlingStrategy == CertHandlingStrategy.TRUST_ALL) {
                Log.d(TAG, "trusting all certificates");
                ((HttpsURLConnection) connection).setHostnameVerifier(nullHostnameVerifier);
            } else if (certificateHandlingStrategy == CertHandlingStrategy.TRUST_CUSTOM) {
                Log.d(TAG, "trusting all only known certificates");
                ((HttpsURLConnection) connection).setHostnameVerifier(listHostnameVerifier);
            }
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

    @UiThread
    void showToast(String msg) {
        Toast.makeText(rootContext, msg, Toast.LENGTH_LONG).show();
    }

    void trustImportedCert() {
        Collection<CaCert> caCerts = providerFacade.query(CaCert.class).all();
        if (caCerts.size() == 1) {
            try {
                CaCert cert = caCerts.iterator().next();
                installCustomCertificate(cert.asCertificate());
                listHostnameVerifier.initToTrustedHosts(cert.validForAsList());
            } catch (Exception e) {
                incorrectCustomCertificate();
            }
        } else {
            showToast("Expected to have exactly 1 custom certificate but found " + caCerts.size());
            incorrectCustomCertificate();
        }
    }

    synchronized void installCustomCertificate(Certificate ca) {
        try {
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            showToast("Error installing certificate - trusting only known certificates" + e.getMessage());
            trustOnlyKnownCerts();
        }
    }

    private void incorrectCustomCertificate() {
        showToast("The CA is not correct - trusting only known certificates");
        trustOnlyKnownCerts();
    }

    /**
     * This method enables certificate checking.
     */
    private void trustOnlyKnownCerts() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(properSocketFactory);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error trusting system certificate " + e.getMessage());
        }
    }
}