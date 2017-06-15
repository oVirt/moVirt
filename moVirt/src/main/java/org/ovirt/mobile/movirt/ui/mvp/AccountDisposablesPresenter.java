package org.ovirt.mobile.movirt.ui.mvp;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public abstract class AccountDisposablesPresenter<Presenter extends AccountDisposablesPresenter,
        View extends FinishableView> extends DisposablesPresenter<Presenter, View> implements AccountPresenter {

    @Bean
    public AccountRxStore rxStore;

    protected MovirtAccount account;

    @SuppressWarnings("unchecked")
    public Presenter setAccount(MovirtAccount account) {
        this.account = account;
        return (Presenter) this;
    }

    public MovirtAccount getAccount() {
        return account;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Presenter initialize() {
        super.initialize();
        ObjectUtils.requireNotNull(account, "account");

        // finish view if account not valid anymore
        getDisposables().add(rxStore.onRemovedAccountObservable(account)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(acc -> getView().finish()));

        return (Presenter) this;
    }

    public boolean isInstanceOfAccount(MovirtAccount account) {
        return account != null ? account.equals(this.account) : this.account == null;
    }

    protected void finishSafe() {
        final View v = getView();
        if (v != null) {
            v.finish();
        }
    }
}
