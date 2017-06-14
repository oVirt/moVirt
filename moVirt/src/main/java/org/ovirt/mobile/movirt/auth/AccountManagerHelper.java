package org.ovirt.mobile.movirt.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;
import org.ovirt.mobile.movirt.util.resources.Resources;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@EBean(scope = EBean.Scope.Singleton)
public class AccountManagerHelper {
    @SystemService
    AccountManager accountManager;

    @Bean
    CommonMessageHelper commonMessageHelper;

    @Bean
    EnvironmentStore environmentStore;

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @Bean
    Resources resources;

    /**
     * @param name     name of the account
     * @param password password of the account
     * @return created account or null if creation failed
     */
    public MovirtAccount addAccount(String id, String name, String password) {
        Account newAccount = new Account(name, Constants.ACCOUNT_TYPE);
        boolean success = accountManager.addAccountExplicitly(newAccount, password, Bundle.EMPTY);

        MovirtAccount resultAccount = null;
        if (success) {
            accountManager.setUserData(newAccount, AccountProperty.ID.getPackageKey(), id);
            resultAccount = new MovirtAccount(id, newAccount);
            setAccountSyncable(resultAccount, false);
            ContentResolver.setSyncAutomatically(newAccount, OVirtContract.CONTENT_AUTHORITY, false);
            ContentResolver.removePeriodicSync(newAccount, OVirtContract.CONTENT_AUTHORITY, Bundle.EMPTY);
        }

        return resultAccount;
    }

    public void updatePeriodicSync(MovirtAccount account, boolean syncEnabled) {
        ObjectUtils.requireNotNull(account, "account");
        String authority = OVirtContract.CONTENT_AUTHORITY;
        Bundle bundle = Bundle.EMPTY;

        try {
            ContentResolver.setSyncAutomatically(account.getAccount(), OVirtContract.CONTENT_AUTHORITY, syncEnabled);

            if (syncEnabled) {
                long intervalInSeconds = environmentStore.getSharedPreferencesHelper(account).getPeriodicSyncInterval() * (long) Constants.SECONDS_IN_MINUTE;
                ContentResolver.addPeriodicSync(account.getAccount(), authority, bundle, intervalInSeconds);
            } else {
                ContentResolver.removePeriodicSync(account.getAccount(), authority, bundle);
            }
        } catch (AccountDeletedException ignored) {
        }
    }

    public boolean isPeriodicSyncable(MovirtAccount account) throws AccountDeletedException {
        return account != null && ContentResolver.getSyncAutomatically(account.getAccount(), OVirtContract.CONTENT_AUTHORITY);
    }

    public boolean isSyncable(MovirtAccount account) {
        return account != null && ContentResolver.getIsSyncable(account.getAccount(), OVirtContract.CONTENT_AUTHORITY) == 1;
    }

    public void setAccountSyncable(MovirtAccount account, boolean syncable) {
        ContentResolver.setIsSyncable(account.getAccount(), OVirtContract.CONTENT_AUTHORITY, syncable ? 1 : 0);
    }

    /**
     * Helper method to trigger an immediate sync ("refreshAccounts").
     * <p>
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refreshAccounts" button.
     * <p>
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public void triggerRefresh(MovirtAccount account) {
        Account acc = account.getAccount();
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        // cancel sync because account will not sync if similar sync is already in progress
        if (ContentResolver.isSyncPending(acc, OVirtContract.CONTENT_AUTHORITY) ||
                ContentResolver.isSyncActive(acc, OVirtContract.CONTENT_AUTHORITY)) {
            ContentResolver.cancelSync(acc, OVirtContract.CONTENT_AUTHORITY);
        }

        ContentResolver.requestSync(acc, OVirtContract.CONTENT_AUTHORITY, b);
    }

    public MovirtAccount asMoAccount(Account account) throws AccountDeletedException {
        final String id = accountManager.getUserData(account, AccountProperty.ID.getPackageKey());
        if (StringUtils.isEmpty(id)) {
            throw new IllegalStateException("Incompatible account from old moVirt version.");
        }
        return new MovirtAccount(id, account);
    }

    public Set<MovirtAccount> getAllAccounts() {
        try {
            final Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
            Set<MovirtAccount> movirtAccounts = new HashSet<>(accounts.length);
            for (Account account : accounts) {
                try {
                    movirtAccounts.add(asMoAccount(account));
                } catch (IllegalStateException incompatibleAccount) {
                    removeAccount(new MovirtAccount("", account), null); // remove old account
                }
            }

            return movirtAccounts;
        } catch (SecurityException e) {
            commonMessageHelper.showError(ErrorType.NORMAL, e);
            return Collections.emptySet();
        }
    }

    /**
     * This method should be called only from Singletons so the lifecycle
     * is tied with the application, so we don't have to cleanup the listeners
     *
     * @param callback to be called on accounts updated
     */
    public void addOnAccountsUpdatedListener(OnAccountsUpdatedListener callback) {
        try {
            OnAccountsUpdateListener listener = accounts -> {
                Set<MovirtAccount> filtered = new HashSet<>();
                for (Account account : accounts) {
                    if (Constants.ACCOUNT_TYPE.equals(account.type)) {
                        try {
                            filtered.add(asMoAccount(account));
                        } catch (IllegalStateException incompatibleAccount) {
                            removeAccount(new MovirtAccount("", account), null); // remove old account
                        }
                    }
                }
                callback.onAccountsUpdated(filtered);
            };

            accountManager.addOnAccountsUpdatedListener(listener, null, true);
        } catch (SecurityException e) {
            commonMessageHelper.showError(ErrorType.NORMAL, resources.getMissingAccountsPermissionError());
        }
    }

    public void removeAccount(MovirtAccount account, AccountRemovedListener callback) {
        System.out.println(account);
        if (Build.VERSION.SDK_INT < 22) {
            accountManager.removeAccount(account.getAccount(), future -> {
                try {
                    boolean result = future.getResult(Constants.REMOVE_ACCOUNT_CALLBACK_TIMEOUT, TimeUnit.SECONDS);
                    if (callback != null) {
                        callback.onRemoved(result);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        try {
                            callback.onRemoved(false);
                        } catch (Exception ignore) {
                        }
                    }
                }
            }, null);
        } else {
            accountManager.removeAccount(account.getAccount(), null, future -> {
                try {
                    boolean result = future.getResult(Constants.REMOVE_ACCOUNT_CALLBACK_TIMEOUT, TimeUnit.SECONDS).getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                    if (callback != null) {
                        callback.onRemoved(result);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        try {
                            callback.onRemoved(false);
                        } catch (Exception ignore) {
                        }
                    }
                }
            }, null);
        }
    }

    public interface AccountRemovedListener {
        void onRemoved(boolean success);
    }

    public interface OnAccountsUpdatedListener {

        void onAccountsUpdated(Set<MovirtAccount> accounts);
    }
}
