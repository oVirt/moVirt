package org.ovirt.mobile.movirt.util.message;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

import java.net.MalformedURLException;
import java.net.URL;

@EBean
public class MessageHelper extends CommonMessageHelper {

    @Bean
    ProviderFacade provider;

    @Bean
    NotificationHelper notificationHelper;

    private AccountPropertiesManager propertiesManager;

    private SharedPreferencesHelper sharedPreferencesHelper;

    public MessageHelper setPropertiesManager(AccountPropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
        return this;
    }

    public MessageHelper setSharedPreferencesHelper(SharedPreferencesHelper sharedPreferencesHelper) {
        this.sharedPreferencesHelper = sharedPreferencesHelper;
        return this;
    }

    @Override
    protected MovirtAccount getAccount() {
        if (propertiesManager == null || sharedPreferencesHelper == null) {
            throw new IllegalArgumentException("Message helper is not initialized!");
        }

        return propertiesManager.getManagedAccount();
    }

    @Override
    protected String getConnectionDetails() {
        String token = propertiesManager.peekAuthToken();
        if (token == null) {
            token = context.getString(R.string.rest_error_detail_token_missing);
        }

        StringBuilder certificate = new StringBuilder();
        String apiUrl = propertiesManager.getApiUrl();
        if (apiUrl != null) {
            try {
                URL url = new URL(apiUrl);
                CertHandlingStrategy certHandlingStrategy = propertiesManager.getCertHandlingStrategy();

                if (url.getProtocol().equalsIgnoreCase("https")) {
                    certificate.append("\n").append(context.getString(R.string.rest_error_detail_certificate_strategy,
                            certHandlingStrategy.toString()));
                }
                if (certHandlingStrategy == CertHandlingStrategy.TRUST_CUSTOM) {
                    boolean hasCert = propertiesManager.getCertificateChain().length > 0;
                    certificate.append("\n\t")
                            .append(context.getString(hasCert ? R.string.rest_error_detail_certificate_stored :
                                    R.string.rest_error_detail_certificate_missing));
                }
            } catch (MalformedURLException e) {
                apiUrl = context.getString(R.string.rest_error_detail_malformed_url, e.getMessage());
            }
        } else {
            apiUrl = context.getString(R.string.rest_error_detail_missing_url);
        }
        return context.getString(R.string.rest_error_details,
                apiUrl, propertiesManager.getUsername(), token, certificate.toString());
    }
}
