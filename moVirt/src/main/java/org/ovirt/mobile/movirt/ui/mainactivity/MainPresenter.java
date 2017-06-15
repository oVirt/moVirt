package org.ovirt.mobile.movirt.ui.mainactivity;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.AllAccounts;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class MainPresenter extends DisposablesPresenter<MainPresenter, MainContract.View>
        implements MainContract.Presenter {

    private static final int CLOSE_DRAWER_DELAY = 300; // ms

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @Bean
    ProviderFacade providerFacade;

    @Bean
    AccountRxStore rxStore;

    @Override
    public MainPresenter initialize() {
        super.initialize();
        Observable<List<Cluster>> clusters = providerFacade.query(Cluster.class)
                .orderBy(Cluster.NAME)
                .asObservable()
                .startWith(Collections.<Cluster>emptyList());

        getDisposables().add(Observable.combineLatest(rxStore.ALL_ACCOUNTS.startWith(AllAccounts.NO_ACCOUNTS), clusters, AccountsClusters::new)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountClusters -> {
                    getView().showAccountsAndClusters(accountClusters.getAssigned());
                    getView().selectActiveSelection(rxStore.getActiveSelection());
                }));

        getDisposables().add(rxStore.ACTIVE_SELECTION
                .distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(selection -> {
                    getView().selectActiveSelection(selection);
                    getView().displayStatus(selection);
                }));

        if (!commonSharedPreferencesHelper.isFirstAccountConfigured()) {
            getView().showAccountDialog();
        }

        return this;
    }

    @Override
    public void onActiveSelectionChanged(ActiveSelection activeSelection) {
        if (!rxStore.getActiveSelection().equals(activeSelection)) {
            rxStore.ACTIVE_SELECTION.onNext(activeSelection);
            Observable.timer(CLOSE_DRAWER_DELAY, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> getView().hideDrawer());
        }
    }

    @Override
    public void onLongClickListener(ActiveSelection possibleSelection) {
        if (possibleSelection.isNotCluster()) {
            if (possibleSelection.isAllAccounts()) {
                getView().startEditAccountsActivity();
            } else {
                getView().startAccountSettingsActivity(possibleSelection.getAccount());
            }
        }
    }

    @Override
    public void editTriggers() {
        getView().startEditTriggersActivity(rxStore.getActiveSelection());
    }

    private class AccountsClusters {
        final AllAccounts allAccounts;
        final List<Cluster> clusters;

        public AccountsClusters(AllAccounts allAccounts, List<Cluster> clusters) {
            this.allAccounts = allAccounts;
            this.clusters = clusters;
        }

        public SortedMap<MovirtAccount, List<Cluster>> getAssigned() {
            SortedMap<MovirtAccount, List<Cluster>> map = new TreeMap<>();

            for (MovirtAccount account : allAccounts.getAccounts()) {
                map.put(account, new ArrayList<>());
            }

            for (Cluster cluster : clusters) {
                MovirtAccount account = allAccounts.getAccountById(cluster.getAccountId());
                if (account != null) {
                    map.get(account).add(cluster);
                }
            }

            return map;
        }
    }
}
