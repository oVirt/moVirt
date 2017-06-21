package org.ovirt.mobile.movirt.ui.account;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.AllAccounts;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class EditAccountsPresenter extends DisposablesPresenter<EditAccountsPresenter, EditAccountsContract.View>
        implements EditAccountsContract.Presenter {
    @Bean
    AccountRxStore rxStore;

    @Bean
    EnvironmentStore envStore;

    @Override
    public EditAccountsPresenter initialize() {
        super.initialize();

        getDisposables().add(Observable.combineLatest(rxStore.ALL_ACCOUNTS.startWith(AllAccounts.NO_ACCOUNTS), rxStore.ACTIVE_SELECTION, Wrapper::new)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wrapper -> {

                    Collection<MovirtAccount> accs = wrapper.accounts.getAccounts();
                    List<AccountWrapper> wrappers = new ArrayList<>(accs.size());

                    for (MovirtAccount account : accs) {
                        try {
                            wrappers.add(new AccountWrapper(account, envStore.getVersion(account)));
                        } catch (AccountDeletedException ignore) {
                        }
                    }

                    getView().showAccounts(wrappers, wrapper.activeSelection);
                }));

        return this;
    }

    @Override
    public void accountClicked(MovirtAccount account) {
        getView().startSettingsAccountActivity(account);
    }

    @Override
    public void addAccount() {
        getView().startAddAccountActivity();
    }

    @Override
    public void deleteAccount(MovirtAccount account) {
        rxStore.removeAccount(account);
    }

    class Wrapper {
        AllAccounts accounts;
        ActiveSelection activeSelection;

        public Wrapper(AllAccounts accounts, ActiveSelection activeSelection) {
            this.accounts = accounts;
            this.activeSelection = activeSelection;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Wrapper)) return false;

            Wrapper wrapper = (Wrapper) o;

            if (accounts != null ? !accounts.equals(wrapper.accounts) : wrapper.accounts != null)
                return false;
            return activeSelection != null ? activeSelection.equals(wrapper.activeSelection) : wrapper.activeSelection == null;
        }

        @Override
        public int hashCode() {
            int result = accounts != null ? accounts.hashCode() : 0;
            result = 31 * result + (activeSelection != null ? activeSelection.hashCode() : 0);
            return result;
        }
    }
}
