package org.ovirt.mobile.movirt.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.sync.OVirtClient;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;


@EBean
public class MovirtAuthenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_NAME = "oVirt";

    public static final String ACCOUNT_TYPE = "org.ovirt.mobile.movirt.authenticator";

    public static final String AUTH_TOKEN_TYPE = "org.ovirt.mobile.movirt.token";

    public static final String USER_NAME = "org.ovirt.mobile.movirt.username";

    public static final String API_URL = "org.ovirt.mobile.movirt.apiurl";

    public static final String HAS_ADMIN_PERMISSIONS = "org.ovirt.mobile.movirt.adminpermissionsm";

    public static final String DISABLE_HTTPS = "org.ovirt.mobile.movirt.disablehttps";

    public static final String ENFORCE_HTTP_BASIC = "org.ovirt.mobile.movirt.enforceHttpBasic";

    public static final String USE_DOCTOR_REST = "org.ovirt.mobile.movirt.useDoctorRest";

    public static final String DOCTOR_REST_URL = "org.ovirt.mobile.movirt.doctorRestUrl";

    public static final String DOCTOR_MQTT_URL = "org.ovirt.mobile.movirt.doctorMqttUrl";

    public static final Account MOVIRT_ACCOUNT = new Account(MovirtAuthenticator.ACCOUNT_NAME, MovirtAuthenticator.ACCOUNT_TYPE);

    @Bean
    OVirtClient client;

    @SystemService
    AccountManager accountManager;

    private Context context;

    public MovirtAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s2, String[] strings, Bundle options) throws NetworkErrorException {
        if (accountConfigured()) {
            showToast("Only one moVirt account is allowed per device");
            return null;
        } else {
            return createAccountActivity(accountAuthenticatorResponse);
        }
    }

    @UiThread
    void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            final String username = getUserName();
            final String password = getPassword();
            if (username != null && password != null) {
                authToken = client.login(getApiUrl(), username, password, disableHttps(), hasAdminPermissions());
                if(!TextUtils.isEmpty(authToken)) {
                    accountManager.setAuthToken(account, authTokenType, authToken);
                }
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        return createAccountActivity(accountAuthenticatorResponse);
    }

    private Bundle createAccountActivity(AccountAuthenticatorResponse accountAuthenticatorResponse) {
        final Intent intent = new Intent(context, AuthenticatorActivity_.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

    public String getApiUrl() {
        return read(API_URL);
    }

    public String getUserName() {
        return read(USER_NAME);
    }

    public String getPassword() {
        return accountManager.getPassword(MOVIRT_ACCOUNT);
    }

    public Boolean hasAdminPermissions() {
        return read(HAS_ADMIN_PERMISSIONS, false);
    }

    public Boolean disableHttps() {
        return read(DISABLE_HTTPS, false);
    }

    public Boolean enforceBasicAuth() {
        return read(ENFORCE_HTTP_BASIC, false);
    }

    public Boolean useDoctorRest() {
        return read(USE_DOCTOR_REST, false);
    }

    public String getDoctorRestUrl() {
        return read(DOCTOR_REST_URL);
    }

    public String getDoctorMqttUrl() {
        return read(DOCTOR_MQTT_URL);
    }

    public boolean accountConfigured() {
        return accountManager.getAccountsByType(ACCOUNT_TYPE).length > 0;
    }

    private Boolean read(String id, boolean defRes) {
        String res = accountManager.getUserData(MOVIRT_ACCOUNT, id);
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return Boolean.valueOf(res);
    }

    private String read(String id) {
        return accountManager.getUserData(MOVIRT_ACCOUNT, id);
    }
}
