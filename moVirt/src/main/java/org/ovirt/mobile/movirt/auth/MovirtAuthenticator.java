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

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.ui.account.AddAccountActivity_;
import org.ovirt.mobile.movirt.ui.account.EditAccountsActivity_;

@EBean(scope = EBean.Scope.Singleton)
public class MovirtAuthenticator extends AbstractAccountAuthenticator {

    @SystemService
    AccountManager accountManager;

    @Bean
    AccountManagerHelper accountManagerHelper;

    @Bean
    EnvironmentStore environmentStore;

    @RootContext
    Context context;

    public MovirtAuthenticator(Context context) {
        super(context);
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
            try {
                final MovirtAccount movirtAccount = accountManagerHelper.asMoAccount(account);
                final AccountPropertiesManager propertiesManager = environmentStore.getAccountPropertiesManager(movirtAccount);
                final String username = propertiesManager.getUsername();
                final String password = propertiesManager.getPassword();
                if (username != null && password != null) {
                    try {
                        if (!environmentStore.isLoginInProgress(movirtAccount)) { // do not attempt to login while user tries
                            authToken = environmentStore.getLoginClient(movirtAccount).login(username, password);
                        }
                    } catch (Exception x) { // do not fail on bad login info
                    }

                    if (!TextUtils.isEmpty(authToken)) {
                        accountManager.setAuthToken(account, authTokenType, authToken);
                    }
                }
            } catch (AccountDeletedException ignore) {
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
}
