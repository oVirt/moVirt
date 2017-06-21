package org.ovirt.mobile.movirt.auth.account;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.AccountManagerHelper;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.AllAccounts;
import org.ovirt.mobile.movirt.auth.account.data.CertificateDownloadStatus;
import org.ovirt.mobile.movirt.auth.account.data.LoginStatus;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.account.data.SelectedAccountRemoved;
import org.ovirt.mobile.movirt.auth.account.data.SelectionClusters;
import org.ovirt.mobile.movirt.auth.account.data.SyncStatus;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper.StoredActiveSelection;

@EBean(scope = EBean.Scope.Singleton)
public class AccountRxStore {

    public final Subject<AllAccounts> ALL_ACCOUNTS = BehaviorSubject.<AllAccounts>create().toSerialized();

    public final Subject<ActiveSelection> ACTIVE_SELECTION = BehaviorSubject.createDefault(ActiveSelection.ALL_ACTIVE).toSerialized();

    public final Subject<MovirtAccount> REMOVED_ACCOUNT = PublishSubject.<MovirtAccount>create().toSerialized();

    public final Subject<LoginStatus> LOGIN_STATUS = PublishSubject.<LoginStatus>create().toSerialized();

    public final Subject<CertificateDownloadStatus> CERTIFICATE_DOWNLOAD_STATUS = PublishSubject.<CertificateDownloadStatus>create().toSerialized();

    public final Subject<SyncStatus> SYNC_STATUS = PublishSubject.<SyncStatus>create().toSerialized();

    @RootContext
    Context context;

    @Bean
    AccountManagerHelper accountManagerHelper;

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @Bean
    CommonMessageHelper messageHelper;

    @Bean
    EnvironmentStore environmentStore;

    @Bean
    ProviderFacade providerFacade;

    @AfterInject
    void init() {
        // refresh account even though we add listener later
        Set<MovirtAccount> accounts = refreshAccounts();

        // load active account from preferences
        final StoredActiveSelection storedSelection = commonSharedPreferencesHelper.getActiveSelection();
        for (MovirtAccount account : accounts) {
            if (account.getId().equals(storedSelection.accountId)) {
                ACTIVE_SELECTION.onNext(new ActiveSelection(account, storedSelection.clusterId));
            }
        }

        Observable.combineLatest(providerFacade.query(Cluster.class).asObservable(),
                ACTIVE_SELECTION.distinctUntilChanged(), SelectionClusters::new)
                .subscribeOn(Schedulers.computation())
                .subscribe(selectionClusters -> {
                    ActiveSelection selection = selectionClusters.getActiveSelection();
                    // preserve active one in preferences
                    commonSharedPreferencesHelper.setActiveSelection(new StoredActiveSelection(selection));

                    if (selection.isCluster()) {
                        // check if cluster name changed
                        for (Cluster cluster : selectionClusters.getClusters()) {
                            if (selection.isCluster(cluster.getId())) {
                                if (!selection.isClusterName(cluster.getName())) {
                                    ACTIVE_SELECTION.onNext(new ActiveSelection(selection.getAccount(),
                                            selection.getClusterId(), cluster.getName()));
                                }
                                return;
                            }
                        }
                        ACTIVE_SELECTION.onNext(ActiveSelection.ALL_ACTIVE); // cluster deleted
                    }
                });

        ALL_ACCOUNTS.map(allAccounts -> accounts.size())
                .distinctUntilChanged()
                .filter(size -> size == 0) // detect only change when all deleted, it is set to true on account creation
                .subscribeOn(Schedulers.computation())
                .subscribe(accountSize -> commonSharedPreferencesHelper.setFirstAccountConfigured(false));

        Observable.combineLatest(REMOVED_ACCOUNT, ACTIVE_SELECTION, SelectedAccountRemoved::new)
                .filter(SelectedAccountRemoved::isRemoved)
                .subscribeOn(Schedulers.computation())
                .subscribe(removed -> ACTIVE_SELECTION.onNext(ActiveSelection.ALL_ACTIVE));

        // listen for system changes on accounts
        accountManagerHelper.addOnAccountsUpdatedListener(accs -> ALL_ACCOUNTS.onNext(AllAccounts.newInstance(accs)));
    }

    /**
     * @param name name of the account
     * @return created account
     * @throws IllegalArgumentException if any constraint violated
     */
    @NonNull
    public MovirtAccount addAccount(String name) throws IllegalArgumentException {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Name shouldn't be empty.");
        }

        name = name.trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name shouldn't consist of only whitespace characters.");
        }

        if (name.length() > Constants.MAX_ACCOUNT_NAME_LENTH) {
            throw new IllegalArgumentException("Name shouldn't be longer than 20 characters.");
        }

        String id = UUID.randomUUID().toString();

        for (MovirtAccount acc : accountManagerHelper.getAllAccounts()) {
            if (acc.getName().equals(name)) {
                throw new IllegalArgumentException("Account with this name already exists.");
            } else if (acc.getId().equals(id)) {
                throw new IllegalArgumentException("Account with this id already exists.");
            }
        }

        MovirtAccount account = accountManagerHelper.addAccount(id, name, "");
        if (account == null) {
            throw new IllegalArgumentException("Could not create an account.");
        }
        refreshAccounts();

        return account;
    }

    public void removeAccount(MovirtAccount account) {
        // try to destroy it and notify others before completely removing it
        environmentStore.removeEnvironment(account);
        accountManagerHelper.removeAccount(account, success -> {  // refreshAccounts callback gets called
            if (success) {
                refreshAccounts();
            } else {
                messageHelper.showError(String.format("Could not remove account %s", account == null ? null : account.getName()));
            }
        });
    }

    public Observable<MovirtAccount> onRemovedAccountObservable(MovirtAccount account) {
        Observable<MovirtAccount> result = REMOVED_ACCOUNT;

        if (!environmentStore.hasEnvironment(account)) {
            result = result.startWith(account);
        }

        return result.filter(acc -> acc.equals(account));
    }

    public Observable<LoginStatus> isLoginInProgressObservable(MovirtAccount account) throws AccountDeletedException {
        return LOGIN_STATUS.startWith(new LoginStatus(account, environmentStore.isLoginInProgress(account)))
                .filter(status -> status.isAccount(account));
    }

    public Observable<SyncStatus> isSyncInProgressObservable(MovirtAccount account) throws AccountDeletedException {
        if (account == null) {
            return environmentStore.ANY_ACCOUNT_IN_SYNC.map(SyncStatus::new);
        } else {
            return SYNC_STATUS.startWith(new SyncStatus(account, environmentStore.isInSync(account)))
                    .filter(status -> status.isAccount(account));
        }
    }

    public ActiveSelection getActiveSelection() {
        return ACTIVE_SELECTION.blockingFirst(ActiveSelection.ALL_ACTIVE);
    }

    public Collection<MovirtAccount> getAllAccounts() {
        final AllAccounts accounts = ALL_ACCOUNTS.blockingFirst(AllAccounts.NO_ACCOUNTS);
        return accounts.getAccounts();
    }

    public AllAccounts getAllAccountsWrapped() {
        return ALL_ACCOUNTS.blockingFirst(AllAccounts.NO_ACCOUNTS);
    }

    /**
     * Accounts change could have been initiated by user from system settings
     */
    private Set<MovirtAccount> refreshAccounts() {
        Set<MovirtAccount> accounts = accountManagerHelper.getAllAccounts();
        ALL_ACCOUNTS.onNext(AllAccounts.newInstance(accounts));
        return accounts;
    }
}
