package org.ovirt.mobile.movirt.ui.hosts;

import android.os.RemoteException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.ClusterAndEntity;
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.mvp.AccountDisposablesProgressBarPresenter;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class HostDetailPresenter extends AccountDisposablesProgressBarPresenter<HostDetailPresenter, HostDetailContract.View>
        implements HostDetailContract.Presenter {

    private String hostId;

    @Bean
    ProviderFacade providerFacade;

    @Bean
    EnvironmentStore environmentStore;

    @Override
    public HostDetailPresenter setHostId(String id) {
        hostId = id;
        return this;
    }

    @Override
    public HostDetailPresenter initialize() {
        ObjectUtils.requireNotNull(hostId, "hostId");
        super.initialize();

        getDisposables().add(providerFacade.query(Host.class)
                .where(StorageDomain.ID, hostId)
                .singleAsObservable()
                .switchMap(host -> providerFacade.query(Cluster.class)
                        .where(Cluster.ID, host.getClusterId())
                        .singleAsObservable()
                        .map(cluster -> new ClusterAndEntity<>(host, cluster))
                        .startWith(new ClusterAndEntity<>(host, null)))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wrapper -> {
                    getView().displayTitle(wrapper.entity.getName());
                    getView().displayHostStatus(wrapper.entity.getStatus());
                    getView().displayStatus(new Selection(account, wrapper.getClusterName(), wrapper.entity.getName()));
                }));

        return this;
    }

    @Background
    @Override
    public void activate() {
        environmentStore.safeOvirtClientCall(account,
                client -> client.activateHost(hostId, new SyncHostResponse()));
    }

    @Override
    @Background
    public void deactivate() {
        environmentStore.safeOvirtClientCall(account,
                client -> client.dectivateHost(hostId, new SyncHostResponse()));
    }

    @Override
    public void destroy() {
        super.destroy();
        environmentStore.safeEnvironmentCall(account,
                env -> env.getEventProviderHelper().deleteTemporaryEvents());
    }

    /**
     * Refreshes Host upon success
     */
    private class SyncHostResponse extends SimpleResponse<Void> {
        @Override
        public void onResponse(Void aVoid) throws RemoteException {
            environmentStore.safeEntityFacadeCall(account, Host.class,
                    facade -> facade.syncOne(new ProgressBarResponse<>(HostDetailPresenter.this), hostId));
        }
    }
}
