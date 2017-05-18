package org.ovirt.mobile.movirt.ui.storage;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.mvp.AccountDisposablesPresenter;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.resources.Resources;

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

    @Bean
    Resources resources;

    @Override
    public StorageDomainDetailPresenter setStorageDomainId(String id) {
        sdId = id;
        return this;
    }

    @Override
    public StorageDomainDetailPresenter initialize() {
        super.initialize();
        ObjectUtils.requireNotNull(sdId, "sdId");

        getView().displayStatus(ActiveSelection.getDescription(account, "", resources.getStorageDomain()));

        getDisposables().add(providerFacade.query(StorageDomain.class)
                .where(StorageDomain.ID, sdId)
                .singleAsObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(storageDomain -> getView().displayTitle(storageDomain.getName())));

        return this;
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            environmentStore.getEventProviderHelper(account).deleteTemporaryEvents();
        } catch (AccountDeletedException ignore) {
        }
    }
}
