package org.ovirt.mobile.movirt.ui.dashboard;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class DashboardPresenter extends DisposablesPresenter<DashboardPresenter, DashboardContract.View>
        implements DashboardContract.Presenter {

    @Bean
    AccountRxStore rxStore;

    @Override
    public DashboardPresenter initialize() {
        super.initialize();

        getDisposables().add(rxStore.ACTIVE_SELECTION
                .distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(selection -> {
                    getView().displayStatus(selection);
                }));

        return this;
    }
}
