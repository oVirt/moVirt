package org.ovirt.mobile.movirt.rest.client.requestfactory;

import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.rest.client.requestfactory.verifier.ListHostnameVerifier;
import org.ovirt.mobile.movirt.rest.client.requestfactory.verifier.NullHostnameVerifier;
import org.ovirt.mobile.movirt.util.CertHelper;
import org.ovirt.mobile.movirt.util.DestroyableListeners;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@EBean
public class OvirtSimpleClientHttpRequestFactory extends SimpleClientHttpRequestFactory implements AccountEnvironment.EnvDisposable {

    private static final String TAG = OvirtSimpleClientHttpRequestFactory.class.getSimpleName();

    private MessageHelper messageHelper;

    private AccountPropertiesManager propertiesManager;

    private CertHandlingStrategy certificateHandlingMode = null;

    private DestroyableListeners listeners;

    private SSLSocketFactory sslSocketFactory;

    @Bean
    NullHostnameVerifier nullHostnameVerifier;

    @Bean
    ListHostnameVerifier listHostnameVerifier;

    public OvirtSimpleClientHttpRequestFactory setTimeout(int seconds) {
        setConnectTimeout(1000 * seconds);
        return this;
    }

    public OvirtSimpleClientHttpRequestFactory init(AccountPropertiesManager propertiesManager, MessageHelper messageHelper) {
        ObjectUtils.requireNotNull(propertiesManager, "propertiesManager");
        ObjectUtils.requireNotNull(messageHelper, "messageHelper");
        this.propertiesManager = propertiesManager;
        this.messageHelper = messageHelper;

        listeners = new DestroyableListeners(propertiesManager)
                .notifyAndRegisterListener(new AccountProperty.CertHandlingStrategyListener() {
                    @Override
                    public void onPropertyChange(CertHandlingStrategy certHandlingStrategy) {
                        onHandlingModeChange(certHandlingStrategy, null);
                    }
                }).registerListener(new AccountProperty.CertificateChainListener() {
                    @Override
                    public void onPropertyChange(Cert[] certificates) {
                        onHandlingModeChange(certificateHandlingMode, certificates);
                    }
                }).notifyAndRegisterListener(new AccountProperty.ValidHostnameListListener() {
                    @Override
                    public void onPropertyChange(String[] validHostnameList) {
                        listHostnameVerifier.setTrustedHosts(validHostnameList);
                    }
                });

        return this;
    }

    @Override
    public void dispose() {
        listeners.destroy();
    }

    private void onHandlingModeChange(CertHandlingStrategy certificateHandlingMode, Cert[] certChain) {
        this.certificateHandlingMode = certificateHandlingMode;

        switch (certificateHandlingMode) {
            case TRUST_SYSTEM:
                trustOnlyKnownCerts();
                break;
            case TRUST_CUSTOM:
                if (certChain == null) {
                    certChain = propertiesManager.getCertificateChain();
                }
                trustImportedCert(certChain);
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
            HttpsURLConnection httpsConn = ((HttpsURLConnection) connection);

            if (certificateHandlingMode == CertHandlingStrategy.TRUST_ALL) {
                Log.d(TAG, "trusting all certificates");
                httpsConn.setHostnameVerifier(nullHostnameVerifier);
            } else if (certificateHandlingMode == CertHandlingStrategy.TRUST_CUSTOM) {
                httpsConn.setHostnameVerifier(listHostnameVerifier);
            }
            if (sslSocketFactory == null) {
                trustOnlyKnownCerts();
            }
            httpsConn.setSSLSocketFactory(sslSocketFactory);
        }

        super.prepareConnection(connection, httpMethod);
    }

    private void trustImportedCert(Cert[] certChain) {
        try {
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            // try to add certificate - if adding fails do not trust anything
            Cert cert = (certChain.length == 0) ? null : certChain[certChain.length - 1];
            if (cert != null) {
                Certificate certificate = cert.asCertificate();
                if (CertHelper.isCA(certificate)) {
                    keyStore.setCertificateEntry("ca_" + propertiesManager.getManagedAccount().getId(), certificate);
                }
            }

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
            sslSocketFactory = context.getSocketFactory();
        } catch (Exception e) {
            messageHelper.showError(ErrorType.NORMAL, e,
                    "Error installing custom certificates - trusting system certificates!");
            try {
                propertiesManager.setCertHandlingStrategy(CertHandlingStrategy.TRUST_SYSTEM);
            } catch (AccountDeletedException ignored) {
            }
        }
    }

    /**
     * This method enables certificate checking.
     */
    private void trustOnlyKnownCerts() {
        try {
            sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        } catch (Exception e) {
            messageHelper.showError(e);
        }
    }

    /**
     * Trust every server - don't check for any certificate
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
            sslSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
            messageHelper.showError(e);
        }
    }
}
