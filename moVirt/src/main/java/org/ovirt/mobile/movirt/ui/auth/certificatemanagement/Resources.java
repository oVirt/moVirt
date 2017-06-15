package org.ovirt.mobile.movirt.ui.auth.certificatemanagement;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.util.resources.StringResources;

import java.net.URL;

@EBean
class Resources extends StringResources {

    String getMaxVisibleCertsReachedError(int maxCertificates) {
        return getString(R.string.advanced_authenticator_error_max_visible_certs_reached, maxCertificates);
    }

    String badlyFormattedCertsError() {
        return getString(R.string.badly_formatted_certs_error);
    }

    String notSelfSignedEngineError() {
        return getString(R.string.not_self_signed_engine_error);
    }

    String deletedCertsChainMessage() {
        return getString(R.string.deleted_cert_chain);
    }

    String wrongUrl() {
        return getString(R.string.wrong_url_in_connection_settings);
    }

    String invalidApiUrl() {
        return getString(R.string.api_url_not_valid);
    }

    String validUrlToConfigureCertsError() {
        return getString(R.string.valid_url_to_configure_certs_error);
    }

    String checkedUrlsToString(URL[] urls) {
        StringBuilder urlsChecked = new StringBuilder("Urls checked:\n\n");
        for (int u = 0; u < urls.length; u++) {
            urlsChecked.append(urls[u]);
            if (u != urls.length - 1) {
                urlsChecked.append(",\n");
            }
        }
        urlsChecked.append("\n");
        return urlsChecked.toString();
    }

    public String noFileSelected() {
        return getString(R.string.no_file_selected);
    }
}
