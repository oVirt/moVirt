package org.ovirt.mobile.movirt.ui.dashboard.boxfragment;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;
import org.ovirt.mobile.movirt.util.Disposables;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.ovirt.mobile.movirt.ui.dashboard.DashboardHelper.querySelection;

@EBean
public class DashboardBoxPresenter extends DisposablesPresenter<DashboardBoxPresenter, DashboardBoxContract.View>
        implements DashboardBoxContract.Presenter {

    @Bean
    AccountRxStore rxStore;

    @Bean
    ProviderFacade providerFacade;

    private Disposables boxDisposables = new Disposables();

    @Override
    public DashboardBoxPresenter initialize() {
        super.initialize();

        getDisposables().add(rxStore.ACTIVE_SELECTION
                .distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .subscribe(selection -> {
                    boxDisposables.destroy();

                    subscribeTo(selection, DataCenter.class, BoxDataEntity.DATA_CENTER);
                    subscribeTo(selection, Cluster.class, BoxDataEntity.CLUSTER);
                    subscribeTo(selection, Host.class, BoxDataEntity.HOST);
                    subscribeTo(selection, StorageDomain.class, BoxDataEntity.STORAGE_DOMAIN);
                    subscribeTo(selection, Vm.class, BoxDataEntity.VM);
                    subscribeTo(selection, Event.class, BoxDataEntity.EVENT);
                }));

        return this;
    }

    private <E extends OVirtEntity> void subscribeTo(ActiveSelection selection, Class<E> clazz, BoxDataEntity boxDataEntity) {
        boxDisposables.add(querySelection(providerFacade, clazz, selection)
                .asObservable()
                .map(dataCenters -> new DashboardBoxData(boxDataEntity, dataCenters))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dashboardBoxData -> getView().putData(dashboardBoxData)));
    }

    @Override
    public void destroy() {
        super.destroy();
        boxDisposables.destroy();
    }
}
