package org.ovirt.mobile.movirt.auth.account;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.auth.account.data.ActiveAccount;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@EBean(scope = EBean.Scope.Singleton)
public class AccountRxStore {

    public final Subject<ActiveAccount> ACTIVE_ACCOUNT = BehaviorSubject.createDefault(ActiveAccount.ALL_ACTIVE).toSerialized();

//    public final Subject<Account> REMOVED_ACCOUNTS = PublishSubject.<Account>create().toSerialized();

    public final Subject<Account[]> ALL_ACCOUNTS = BehaviorSubject.createDefault(new Account[]{}).toSerialized();

    @RootContext
    Context context;

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @Bean
    MessageHelper messageHelper;

    @AfterInject
    void init() {
        // load acounts
        refreshAccounts();

        // load active account from preferences
        final String activeAccountName = commonSharedPreferencesHelper.getActiveAccountName();
        if (!TextUtils.isEmpty(activeAccountName)) {
            for (Account account : authenticator.getAllAccounts()) {
                if (account.name.equals(activeAccountName)) {
                    ACTIVE_ACCOUNT.onNext(new ActiveAccount(account));
                }
            }
        }

        // preserve active one in preferences
        ACTIVE_ACCOUNT.subscribe(activeAccount -> {
            commonSharedPreferencesHelper.setActiveAccountName(activeAccount.getAccountName());
        });
    }

    /**
     * @param name name of the account
     * @return created account
     * @throws IllegalArgumentException if any constraint violated
     */
    @NonNull
    public Account addAccount(String name) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Name shouldn't be empty.");
        }

        if (name.length() > 20) {
            throw new IllegalArgumentException("Name shouldn't be longer than 20 characters.");
        }

        Account account = authenticator.addAccount(name, "");
        if (account == null) {
            throw new IllegalArgumentException("Account with this name already exists.");
        }

        refreshAccounts();

        return account;
    }

    public void removeAccount(Account account) {
        authenticator.removeAccount(account, success -> {
            if (success) {
                refreshAccounts();
                if (ACTIVE_ACCOUNT.blockingFirst(ActiveAccount.ALL_ACTIVE).isAccount(account)) {
                    ACTIVE_ACCOUNT.onNext(ActiveAccount.ALL_ACTIVE);
                }
            } else {
                messageHelper.showError(String.format("Could not remove account %s", account.name));
            }
        });
    }

    private void refreshAccounts() {
        ALL_ACCOUNTS.onNext(authenticator.getAllAccounts());
    }
}
