package org.ovirt.mobile.movirt.auth.account;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.auth.account.data.AccountEnvironment;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

import java.util.HashMap;
import java.util.Map;

@EBean(scope = EBean.Scope.Singleton)
public class EnvironmentStore {

    @RootContext
    Context context;

    @Bean
    AccountRxStore accountRxStore;

    private Map<Account, AccountEnvironment> environmentMap = new HashMap<>();

    @AfterInject
    void init() {
        accountRxStore.ALL_ACCOUNTS.subscribe(accounts -> {
            Map<Account, AccountEnvironment> refreshedMap = new HashMap<>();

            // add new
            for (Account account : accounts) {
                AccountEnvironment persisted = environmentMap.remove(account);
                refreshedMap.put(account, persisted != null ? persisted : new AccountEnvironment(account, context));
            }

            // remove old
            for (AccountEnvironment environment : environmentMap.values()) {
                environment.destroy();
            }

            environmentMap = refreshedMap;
        });
    }

    @NonNull
    public AccountEnvironment getEnvironment(Account account) throws EnvironmentNotFoundException {
        AccountEnvironment environment = environmentMap.get(account);
        if (environment == null) {
            throw new EnvironmentNotFoundException();
        }
        return environment;
    }

    @NonNull
    public SharedPreferencesHelper getSharedPreferencesHelper(Account account) throws EnvironmentNotFoundException {
        return getEnvironment(account).getSharedPreferencesHelper();
    }
}
