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
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyUtils;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.Version;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.ui.auth.AuthenticatorActivity;
import org.ovirt.mobile.movirt.ui.auth.AuthenticatorActivity_;
import org.ovirt.mobile.movirt.util.JsonUtils;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;
import org.ovirt.mobile.movirt.util.message.MessageHelper;

@EBean(scope = EBean.Scope.Singleton)
public class MovirtAuthenticator extends AbstractAccountAuthenticator {

    private static final String ACCOUNT_TYPE = Constants.APP_PACKAGE_DOT + "authenticator";

    private static final Account MOVIRT_ACCOUNT = new Account("oVirt", MovirtAuthenticator.ACCOUNT_TYPE);

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

    public boolean initAccount(String password) {
        boolean initialized = accountManager.addAccountExplicitly(getAccount(), password, Bundle.EMPTY);
        if (initialized) {
            ContentResolver.setIsSyncable(getAccount(), OVirtContract.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(getAccount(), OVirtContract.CONTENT_AUTHORITY, true);
            sharedPreferencesHelper.updatePeriodicSync();
        }
        return initialized;
    }

    public Account getAccount() {
        return MOVIRT_ACCOUNT;
    }

    /**
     * @throws IllegalArgumentException if property is not settable
     */
    public void setResource(AccountProperty property, Object object) {
        Account account = getAccount();

        switch (property) {
            case AUTH_TOKEN:
                if (object == null) {
                    accountManager.invalidateAuthToken(property.getPackageKey(),
                            accountManager.peekAuthToken(account, property.getPackageKey()));
                }
                accountManager.setAuthToken(account, property.getPackageKey(), PropertyUtils.convertToString(object));
                break;
            case PEEK_AUTH_TOKEN:
            case FUTURE_AUTH_TOKEN:
                throw new IllegalArgumentException(property.name() + " cannot be set! Use AUTH_TOKEN.");
            case PASSWORD:
                accountManager.setPassword(account, PropertyUtils.convertToString(object)); // triggers sync in later APIs (Android 6)
                break;
            case USERNAME:
            case PASSWORD_VISIBILITY:
            case API_URL:
            case VERSION:
            case CERT_HANDLING_STRATEGY:
            case HAS_ADMIN_PERMISSIONS:
            case CERTIFICATE_CHAIN:
            case VALID_HOSTNAME_LIST:
            case CUSTOM_CERTIFICATE_LOCATION:
                accountManager.setUserData(account, property.getPackageKey(), PropertyUtils.convertToString(object));
                break;
            default:
                throw new IllegalArgumentException(property.name() + " cannot be set!");
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
                return accountManager.peekAuthToken(account, AccountProperty.AUTH_TOKEN.getPackageKey());
            case FUTURE_AUTH_TOKEN:
                return accountManager.getAuthToken(account, AccountProperty.AUTH_TOKEN.getPackageKey(), null, false, null, null);
            case ACCOUNT_CONFIGURED:
                return accountManager.getAccountsByType(ACCOUNT_TYPE).length > 0;
            case PASSWORD:
                return accountManager.getPassword(account);
            case USERNAME:
            case API_URL:
                return read(property);
            case API_BASE_URL:
                String baseUrl = read(AccountProperty.API_URL);
                return baseUrl == null ? null : baseUrl.replaceFirst("/api$", "");
            case VERSION:
                return getApiVersion(property);
            case CERT_HANDLING_STRATEGY:
                return getCertHandlingStrategy(property);
            case PASSWORD_VISIBILITY:
            case HAS_ADMIN_PERMISSIONS:
            case CUSTOM_CERTIFICATE_LOCATION:
                return read(property, false);
            case CERTIFICATE_CHAIN:
                return getCertificateChain(property);
            case VALID_HOSTNAMES:
                return PropertyUtils.catenateToCsv(getValidHostnames(AccountProperty.VALID_HOSTNAME_LIST));
            case VALID_HOSTNAME_LIST:
                return getValidHostnames(property);
            default:
                return null;
        }
    }

    private Version getApiVersion(AccountProperty property) {
        Version result = readObject(property, Version.class);
        if (result == null) {
            result = new Version();
        }
        return result;
    }

    private String[] getValidHostnames(AccountProperty property) {
        String[] result = readObject(property, String[].class);
        return (result == null) ? new String[]{} : result;
    }

    private Cert[] getCertificateChain(AccountProperty property) {
        Cert[] result = readObject(property, Cert[].class);
        return (result == null) ? new Cert[]{} : result;
    }

    private CertHandlingStrategy getCertHandlingStrategy(AccountProperty property) {
        String strategy = read(property);
        if (TextUtils.isEmpty(strategy)) {
            return CertHandlingStrategy.TRUST_SYSTEM;
        }

        try {
            return CertHandlingStrategy.from(Long.valueOf(strategy));
        } catch (NumberFormatException e) {
            return CertHandlingStrategy.TRUST_SYSTEM;
        }
    }

    private <T> T readObject(AccountProperty property, Class<T> clazz) {
        try {
            return JsonUtils.stringToObject(read(property), clazz);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Boolean read(AccountProperty property, boolean defRes) {
        String res = accountManager.getUserData(getAccount(), property.getPackageKey());
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return Boolean.valueOf(res);
    }

    private String read(AccountProperty property, String defRes) {
        String res = accountManager.getUserData(getAccount(), property.getPackageKey());
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return res;
    }

    private String read(AccountProperty property) {
        return accountManager.getUserData(getAccount(), property.getPackageKey());
    }
}
