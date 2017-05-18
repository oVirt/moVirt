package org.ovirt.mobile.movirt.auth.account;

import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.auth.AccountManagerHelper;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.provider.EventProviderHelper;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@EBean(scope = EBean.Scope.Singleton)
public class EnvironmentStore {

    private final Object LOCK = new Object();

    private final Map<MovirtAccount, AccountEnvironment> environmentMap = new ConcurrentHashMap<>();

    public final Subject<Boolean> ANY_ACCOUNT_IN_SYNC = BehaviorSubject.createDefault(false).toSerialized();

    @RootContext
    Context context;

    @SystemService
    AccountManager accountManager;

    @Bean
    AccountRxStore rxStore;

    @Bean
    ProviderFacade providerFacade;

    @Bean
    AccountManagerHelper accountManagerHelper;

    @AfterInject
    void init() {
        setAccounts(accountManagerHelper.getAllAccounts()); // set atomically

        rxStore.ALL_ACCOUNTS.distinctUntilChanged() // and listen
                .subscribeOn(Schedulers.computation())
                .subscribe(accounts -> setAccounts(accounts.getAccounts()));

        rxStore.LOGIN_STATUS.subscribeOn(Schedulers.computation()).subscribe(loginStatus -> {
            final AccountEnvironment environment = environmentMap.get(loginStatus.getAccount());
            if (environment != null) {
                environment.setLoginInProgress(loginStatus.isInProgress());
            }
        });

        rxStore.CERTIFICATE_DOWNLOAD_STATUS.subscribeOn(Schedulers.computation()).subscribe(status -> {
            final AccountEnvironment environment = environmentMap.get(status.getAccount());
            if (environment != null) {
                environment.setCertificateDownloadInProgress(status.isInProgress());
            }
        });

        rxStore.SYNC_STATUS.subscribeOn(Schedulers.computation()).subscribe(syncStatus -> {
            final AccountEnvironment environment = environmentMap.get(syncStatus.getAccount());
            if (environment != null) {
                environment.setInSync(syncStatus.isInProgress());
            }
            ANY_ACCOUNT_IN_SYNC.onNext(isAnyAccountInSync());
        });
    }

    private void setAccounts(Collection<MovirtAccount> accounts) {
        final Set<MovirtAccount> newDataSet = new HashSet<>(accounts);
        // add new and leave persisted
        synchronized (LOCK) { // this change should be atomic but get from map can be in dirty state
            final Iterator<Map.Entry<MovirtAccount, AccountEnvironment>> envsIterator = environmentMap.entrySet().iterator();

            while (envsIterator.hasNext()) {
                Map.Entry<MovirtAccount, AccountEnvironment> entry = envsIterator.next();
                MovirtAccount account = entry.getKey();
                if (newDataSet.contains(account)) {
                    newDataSet.remove(account);
                } else {
                    envsIterator.remove();
                    rxStore.REMOVED_ACCOUNT.onNext(account);
                    entry.getValue().destroy();
                }
            }

            for (MovirtAccount account : newDataSet) {
                environmentMap.put(account, new AccountEnvironment(context, accountManager, providerFacade, account));
            }
        }
    }

    void removeEnvironment(MovirtAccount account) {
        final AccountEnvironment environment;
        synchronized (LOCK) {
            environment = environmentMap.remove(account);
        }
        if (environment != null) {
            rxStore.REMOVED_ACCOUNT.onNext(account);
            environment.destroy();
        }
    }

    public boolean hasEnvironment(MovirtAccount account) {
        return environmentMap.get(account) != null;
    }

    @NonNull
    public AccountEnvironment getEnvironment(MovirtAccount account) throws AccountDeletedException {
        AccountEnvironment environment = environmentMap.get(account);
        if (environment == null) {
            throw new AccountDeletedException();
        }
        return environment;
    }

    @NonNull
    public Collection<AccountEnvironment> getAllEnvironments() throws AccountDeletedException {
        return Collections.unmodifiableCollection(environmentMap.values());
    }

    public void safeOvirtClientCall(MovirtAccount account, OvirtClientRunner runner) {
        AccountEnvironment environment = environmentMap.get(account);
        if (environment != null) {
            runner.run(environment.getOVirtClient());
        }
    }

    public boolean isLoginInProgress(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).isLoginInProgress();
    }

    public boolean isCertificateDownloadInProgress(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).isCertificateDownloadInProgress();
    }

    public boolean isAnyAccountInSync() throws AccountDeletedException {
        for (AccountEnvironment env : environmentMap.values()) {
            if (env.isInSync()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInSync(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).isInSync();
    }

    @NonNull
    public Version getVersion(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).getVersion();
    }

    @NonNull
    public SharedPreferencesHelper getSharedPreferencesHelper(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).getSharedPreferencesHelper();
    }

    @NonNull
    public AccountPropertiesManager getAccountPropertiesManager(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).getAccountPropertiesManager();
    }

    @NonNull
    public MessageHelper getMessageHelper(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).getMessageHelper();
    }

    @NonNull
    public LoginClient getLoginClient(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).getLoginClient();
    }

    @NonNull
    public EventProviderHelper getEventProviderHelper(MovirtAccount account) throws AccountDeletedException {
        return getEnvironment(account).getEventProviderHelper();
    }

    public interface OvirtClientRunner {
        void run(OVirtClient client);
    }
}
