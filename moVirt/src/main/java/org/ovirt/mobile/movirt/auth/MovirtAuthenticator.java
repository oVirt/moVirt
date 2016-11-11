package org.ovirt.mobile.movirt.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;
import org.ovirt.mobile.movirt.util.JsonUtils;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;
import org.ovirt.mobile.movirt.util.Version;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.properties.AccountProperty;


@EBean
public class MovirtAuthenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_NAME = "oVirt";

    public static final String ACCOUNT_TYPE = "org.ovirt.mobile.movirt.authenticator";

    private static final String AUTH_TOKEN_TYPE = "org.ovirt.mobile.movirt.token";

    private static final String USER_NAME = "org.ovirt.mobile.movirt.username";

    private static final String API_URL = "org.ovirt.mobile.movirt.apiurl";

    private static final String API_VERSION = "org.ovirt.mobile.movirt.apiversion";

    private static final String CERT_HANDLING_STRATEGY = "org.ovirt.mobile.movirt.certhandlingstrategy";

    private static final String HAS_ADMIN_PERMISSIONS = "org.ovirt.mobile.movirt.adminpermissionsm";

    private static final String CUSTOM_CERTIFICATE = "org.ovirt.mobile.movirt.customCertificate";

    private static final Account MOVIRT_ACCOUNT = new Account(MovirtAuthenticator.ACCOUNT_NAME, MovirtAuthenticator.ACCOUNT_TYPE);

    @Bean
    LoginClient loginClient;

    @SystemService
    AccountManager accountManager;

    @Bean
    MessageHelper messageHelper;

    @Bean
    SharedPreferencesHelper sharedPreferencesHelper;

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
        if (getResource(AccountProperty.ACCOUNT_CONFIGURED, Boolean.class)) {
            messageHelper.showToast("Only one moVirt account is allowed per device");
            return null;
        } else {
            return createAccountActivity(accountAuthenticatorResponse);
        }
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        String authToken = accountManager.peekAuthToken(account, authTokenType);
        if (TextUtils.isEmpty(authToken)) {
            final String username = getResource(AccountProperty.USERNAME, String.class);
            final String password = getResource(AccountProperty.PASSWORD, String.class);
            if (username != null && password != null) {
                try {
                    if (!AuthenticatorActivity.isInUserLogin()) { // do not attempt to login while user tries
                        authToken = loginClient.login(username, password);
                    }
                } catch (Exception x) { // do not fail on bad login info
                }

                if (!TextUtils.isEmpty(authToken)) {
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

    public void initAccount(String password) {
        if (accountManager.addAccountExplicitly(getAccount(), password, Bundle.EMPTY)) {
            ContentResolver.setIsSyncable(getAccount(), OVirtContract.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(getAccount(), OVirtContract.CONTENT_AUTHORITY, true);
            sharedPreferencesHelper.updatePeriodicSync();
        }
    }

    public Account getAccount() {
        return MOVIRT_ACCOUNT;
    }

    /**
     * @throws IllegalStateException if property is not settable
     */
    public void setResource(AccountProperty property, Object object) {
        Account account = getAccount();

        switch (property) {
            case AUTH_TOKEN:
                if (object == null) {
                    accountManager.invalidateAuthToken(AUTH_TOKEN_TYPE, accountManager.peekAuthToken(account, AUTH_TOKEN_TYPE));
                }
                accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, ObjectUtils.convertToString(object));
                break;
            case PEEK_AUTH_TOKEN:
            case FUTURE_AUTH_TOKEN:
                throw new IllegalStateException(property.name() + " cannot be set! Use AUTH_TOKEN.");
            case USERNAME:
                accountManager.setUserData(account, USER_NAME, ObjectUtils.convertToString(object));
                break;
            case PASSWORD:
                accountManager.setPassword(account, ObjectUtils.convertToString(object)); // triggers sync in later APIs (Android 6)
                break;
            case API_URL:
                accountManager.setUserData(account, API_URL, ObjectUtils.convertToString(object));
                break;
            case VERSION:
                accountManager.setUserData(account, API_VERSION, ObjectUtils.convertToString(object));
                break;
            case CERT_HANDLING_STRATEGY:
                accountManager.setUserData(account, CERT_HANDLING_STRATEGY, ObjectUtils.convertToString(object));
                break;
            case HAS_ADMIN_PERMISSIONS:
                accountManager.setUserData(account, HAS_ADMIN_PERMISSIONS, ObjectUtils.convertToString(object));
                break;
            case ACCOUNT_CONFIGURED:
            case API_BASE_URL:
                throw new IllegalStateException(property.name() + " cannot be set!");
        }

    }


    @SuppressWarnings("unchecked")
    public <E> E getResource(AccountProperty property, Class<E> clazz) {
        return (E) getResource(property);
    }

    /**
     * Should not throw Exceptions
     */
    public Object getResource(AccountProperty property) {
        Account account = getAccount();

        switch (property) {
            case AUTH_TOKEN: // fallback to non blocking peek, used exclusively by AccountPropertiesManager.setAndNotify
            case PEEK_AUTH_TOKEN:
                return accountManager.peekAuthToken(account, AUTH_TOKEN_TYPE);
            case FUTURE_AUTH_TOKEN:
                return accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, false, null, null);
            case ACCOUNT_CONFIGURED:
                return accountManager.getAccountsByType(ACCOUNT_TYPE).length > 0;
            case USERNAME:
                return read(USER_NAME);
            case PASSWORD:
                return accountManager.getPassword(account);
            case API_URL:
                return read(API_URL);
            case API_BASE_URL:
                String baseUrl = read(API_URL);
                return baseUrl == null ? null : baseUrl.replaceFirst("/api$", "");
            case VERSION:
                return getApiVersion();
            case CERT_HANDLING_STRATEGY:
                return getCertHandlingStrategy();
            case HAS_ADMIN_PERMISSIONS:
                return read(HAS_ADMIN_PERMISSIONS, false);
            default:
                return null;
        }
    }

    private Version getApiVersion() {
        Version result = null;
        try {
            result = JsonUtils.stringToObject(read(API_VERSION), Version.class);
        } catch (Exception ignored) {
        }

        if (result == null) {
            result = new Version();
        }
        return result;
    }

    private CertHandlingStrategy getCertHandlingStrategy() {
        String strategy = read(CERT_HANDLING_STRATEGY);
        if (TextUtils.isEmpty(strategy)) {
            return CertHandlingStrategy.TRUST_SYSTEM;
        }

        try {
            return CertHandlingStrategy.from(Long.valueOf(strategy));
        } catch (NumberFormatException e) {
            return CertHandlingStrategy.TRUST_SYSTEM;
        }
    }

    private Boolean read(String id, boolean defRes) {
        String res = accountManager.getUserData(getAccount(), id);
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return Boolean.valueOf(res);
    }

    private String read(String id, String defRes) {
        String res = accountManager.getUserData(getAccount(), id);
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return res;
    }

    private String read(String id) {
        return accountManager.getUserData(getAccount(), id);
    }

}
