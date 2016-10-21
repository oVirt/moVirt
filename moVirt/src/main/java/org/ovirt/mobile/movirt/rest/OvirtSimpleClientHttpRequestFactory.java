package org.ovirt.mobile.movirt.rest;

import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.CaCert;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.properties.AccountPropertiesManager;
import org.ovirt.mobile.movirt.util.properties.AccountProperty;
import org.ovirt.mobile.movirt.util.properties.PropertyChangedListener;
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

    @Bean
    ProviderFacade providerFacade;

    @Bean
    MessageHelper messageHelper;

    @Bean
    AccountPropertiesManager propertiesManager;

    private CertHandlingStrategy certificateHandlingMode = null;

    @AfterInject
    void initFactory() {
        propertiesManager.notifyAndRegisterListener(AccountProperty.CERT_HANDLING_STRATEGY, new PropertyChangedListener<CertHandlingStrategy>() {
            @Override
            public void onPropertyChange(CertHandlingStrategy property) {
                onHandlingModeChange(property);
            }
        });
    }

    private void onHandlingModeChange(CertHandlingStrategy certificateHandlingMode) {
        this.certificateHandlingMode = certificateHandlingMode;

        switch (certificateHandlingMode) {
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
            if (certificateHandlingMode == CertHandlingStrategy.TRUST_ALL) {
                Log.d(TAG, "trusting all certificates");
                ((HttpsURLConnection) connection).setHostnameVerifier(nullHostnameVerifier);
            } else if (certificateHandlingMode == CertHandlingStrategy.TRUST_CUSTOM) {
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
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
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

    private void trustImportedCert() {
        Collection<CaCert> caCerts = providerFacade.query(CaCert.class).all();
        if (caCerts.size() == 1) {
            try {
                CaCert cert = caCerts.iterator().next();
                installCustomCertificate(cert.asCertificate());
                listHostnameVerifier.initToTrustedHosts(cert.validForAsList());
            } catch (Exception e) {
                e.printStackTrace();
                incorrectCustomCertificate();
            }
        }
    }

    private void installCustomCertificate(Certificate ca) {
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
            messageHelper.showToast("Error installing certificate - trusting only known certificates" + e.getMessage());
            trustOnlyKnownCerts();
        }
    }

    private void incorrectCustomCertificate() {
        messageHelper.showToast("The CA is not correct - trusting only known certificates");
        trustOnlyKnownCerts();
    }

    /**
     * This method enables certificate checking.
     */
    private void trustOnlyKnownCerts() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
