package org.ovirt.mobile.movirt.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyUtils;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.ui.account.AddAccountActivity_;
import org.ovirt.mobile.movirt.ui.account.EditAccountsActivity_;
import org.ovirt.mobile.movirt.ui.auth.AuthenticatorActivity;
import org.ovirt.mobile.movirt.util.JsonUtils;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

import java.util.concurrent.TimeUnit;

@EBean(scope = EBean.Scope.Singleton)
public class MovirtAuthenticator extends AbstractAccountAuthenticator {
    private static final String TAG = MovirtAuthenticator.class.getSimpleName();

    private static final String ACCOUNT_TYPE = Constants.APP_PACKAGE_DOT + "authenticator";

    private static final int REMOVE_ACCOUNT_CALLBACK_TIMEOUT = 3; // better safe than sorry, but shouldn't be needed

    @Bean
    LoginClient loginClient;

    @SystemService
    AccountManager accountManager;

    @Bean
    AccountPropertiesManager accountPropertiesManager;

    @Bean
    MessageHelper messageHelper;

    @Bean
    AccountRxStore accountRxStore;

    @Bean
    EnvironmentStore environmentStore;

    private Context context;

    private Account activeAccount;

    public MovirtAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @AfterInject
    public void init() {
        accountRxStore.ACTIVE_ACCOUNT.subscribe(newActive -> {
            activeAccount = newActive.getAccount();
        });
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s2, String[] strings, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(context, AddAccountActivity_.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
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
        final Intent intent = new Intent(context, EditAccountsActivity_.class);
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

    public Account getActiveAccount() {
        return activeAccount;
    }

    /**
     * @param name     name of the account
     * @param password password of the account
     * @return created account or null if creation failed
     */
    public Account addAccount(String name, String password) {
        Account newAccount = new Account(name, MovirtAuthenticator.ACCOUNT_TYPE);
        boolean success = accountManager.addAccountExplicitly(newAccount, password, Bundle.EMPTY);

        if (success) {
            ContentResolver.setIsSyncable(newAccount, OVirtContract.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(newAccount, OVirtContract.CONTENT_AUTHORITY, false);
            ContentResolver.removePeriodicSync(newAccount, OVirtContract.CONTENT_AUTHORITY, Bundle.EMPTY);
        }

        return success ? newAccount : null;
    }

    public void updatePeriodicSync(Account account) {
        String authority = OVirtContract.CONTENT_AUTHORITY;
        Bundle bundle = Bundle.EMPTY;
        SharedPreferencesHelper preferencesHelper = environmentStore.getSharedPreferencesHelper(account);

        boolean syncEnabled = preferencesHelper.isPeriodicSyncEnabled();
        ContentResolver.setSyncAutomatically(account, OVirtContract.CONTENT_AUTHORITY, syncEnabled);

        if (syncEnabled) {
            long intervalInSeconds = (long) preferencesHelper.getPeriodicSyncInterval() * (long) Constants.SECONDS_IN_MINUTE;
            ContentResolver.addPeriodicSync(account, authority, bundle, intervalInSeconds);
        } else {
            ContentResolver.removePeriodicSync(account, authority, bundle);
        }
    }

    public Account[] getAllAccounts() {
        try {
            return accountManager.getAccountsByType(MovirtAuthenticator.ACCOUNT_TYPE);
        } catch (SecurityException e) {
            messageHelper.showError(ErrorType.NORMAL, e);
            return new Account[]{};
        }
    }

    public void removeAccount(Account account, AccountRemovedCallback callback) {
        if (Build.VERSION.SDK_INT < 22) {
            accountManager.removeAccount(account, future -> {
                try {
                    boolean result = future.getResult(REMOVE_ACCOUNT_CALLBACK_TIMEOUT, TimeUnit.SECONDS);
                    callback.onRemoved(result);
                } catch (Exception e) {
                    callback.onRemoved(false);
                }
            }, null);
        } else {
            accountManager.removeAccount(account, null, future -> {
                try {
                    boolean result = future.getResult(REMOVE_ACCOUNT_CALLBACK_TIMEOUT, TimeUnit.SECONDS).getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                    callback.onRemoved(result);
                } catch (Exception e) {
                    callback.onRemoved(false);
                }
            }, null);
        }
    }

    public interface AccountRemovedCallback {
        void onRemoved(boolean success);
    }

    /**
     * @throws IllegalArgumentException if property is not settable
     */
    public void setResource(AccountProperty property, Object object) {
        Account account = activeAccount;

        if (account == null) {
            return;
        }

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
            case FIRST_LOGIN:
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
        Account account = activeAccount;

        switch (property) {
            case AUTH_TOKEN: // fallback to non blocking peek, used exclusively by AccountPropertiesManager.setAndNotify
            case PEEK_AUTH_TOKEN:
                return account == null ? null : accountManager.peekAuthToken(account, AccountProperty.AUTH_TOKEN.getPackageKey());
            case FUTURE_AUTH_TOKEN:
                return account == null ? null : accountManager.getAuthToken(account, AccountProperty.AUTH_TOKEN.getPackageKey(), null, false, null, null);
            case PASSWORD:
                return account == null ? null : accountManager.getPassword(account);
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
            case FIRST_LOGIN:
                return read(property, true);
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
            result = Version.V3;
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
        String res = activeAccount == null ? null : accountManager.getUserData(activeAccount, property.getPackageKey());
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return Boolean.valueOf(res);
    }

    private String read(AccountProperty property, String defRes) {
        String res = activeAccount == null ? null : accountManager.getUserData(activeAccount, property.getPackageKey());
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return res;
    }

    private String read(AccountProperty property) {
        return activeAccount == null ? null : accountManager.getUserData(activeAccount, property.getPackageKey());
    }
}
