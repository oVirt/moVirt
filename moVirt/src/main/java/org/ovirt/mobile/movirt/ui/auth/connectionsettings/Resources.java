package org.ovirt.mobile.movirt.ui.auth.connectionsettings;

import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.util.resources.StringResources;

import java.util.Arrays;

@EBean
public class Resources extends StringResources {

    public String getLoginMalformedUrlError(String error) {
        return getString(R.string.login_error_invalid_api_url, error, getString(R.string.default_endpoint));
    }

    public String getLoginEmptyPasswordError() {
        return getString(R.string.login_error_empty_password);
    }

    public String getLoginInvalidUsernameError() {
        return getString(R.string.login_error_invalid_username,
                getString(R.string.account_username), getString(R.string.default_username));
    }

    public String getLoginEmptyFieldsError() {
        return getString(R.string.login_error_empty_fields);
    }

    public String getLoginBadAdressSuffixError() {
        return getString(R.string.login_error_bad_address_suffix);
    }

    public String getLoginIncorrectCredentialsError() {
        return getString(R.string.login_error_incorrect_username_password);
    }

    public String getLoginIncorrectIpPortError() {
        return getString(R.string.login_error_incorrect_ip_port);
    }

    public String getLoginTimeoutError() {
        return getString(R.string.login_error_timeout);
    }

    public String getLoginError(String error) {
        return getString(R.string.login_error, error);
    }

    public String getLoginCertificateError(Throwable cause) {
        int ignoreIndex = Arrays
                .asList(context.getResources().getStringArray(R.array.cert_option_keys))
                .indexOf("ignore");
        String certIgnore = context.getResources()
                .getStringArray(R.array.certificate_handling_strategy)[ignoreIndex];
        return getString(R.string.login_error_bad_cert, certIgnore, cause.getMessage());
    }

    public String getLoginEmptyTokenError() {
        return getString(R.string.login_error_empty_token, getString(R.string.certificate_management));
    }

    public String getLoginSuccess() {
        return getString(R.string.login_success);
    }

    public String getLoginAccountDeletedError() {
        return getString(R.string.login_error_no_account);
    }

    public void showCertificateError(MovirtAccount account, String message, String apiUrl) {
        Intent intent = new Intent(Broadcasts.REST_CA_FAILURE);
        intent.putExtra(Broadcasts.Extras.ERROR_ACCOUNT, account);
        intent.putExtra(Broadcasts.Extras.ERROR_REASON, message);
        intent.putExtra(Broadcasts.Extras.ERROR_API_URL, apiUrl);
        context.sendBroadcast(intent);
    }
}
