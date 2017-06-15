package org.ovirt.mobile.movirt.ui.storage;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.mvp.AccountDisposablesPresenter;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class StorageDomainDetailPresenter extends AccountDisposablesPresenter<StorageDomainDetailPresenter, StorageDomainDetailContract.View>
        implements StorageDomainDetailContract.Presenter {
    private String sdId;

    @Bean
    ProviderFacade providerFacade;

    @Bean
    EnvironmentStore environmentStore;

    @Override
    public StorageDomainDetailPresenter setStorageDomainId(String id) {
        sdId = id;
        return this;
    }

    @Override
    public StorageDomainDetailPresenter initialize() {
        super.initialize();
        ObjectUtils.requireNotNull(sdId, "sdId");

        getDisposables().add(providerFacade.query(StorageDomain.class)
                .where(StorageDomain.ID, sdId)
                .singleAsObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(storageDomain -> {
                    getView().displayTitle(storageDomain.getName());
                    getView().displayStatus(new Selection(account, storageDomain.getName()));
                }));

        return this;
    }

    @Override
    public void destroy() {
        super.destroy();
        environmentStore.safeEnvironmentCall(account,
                env -> env.getEventProviderHelper().deleteTemporaryEvents());
    }
}
