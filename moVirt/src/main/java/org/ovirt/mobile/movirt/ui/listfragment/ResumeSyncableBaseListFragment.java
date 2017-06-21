package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtAccountEntity;
import org.ovirt.mobile.movirt.rest.ConnectivityHelper;

import io.reactivex.schedulers.Schedulers;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class ResumeSyncableBaseListFragment<E extends OVirtAccountEntity> extends AccountFacadeBaseListFragment<E> {

    @InstanceState
    protected boolean synced = false;

    @Bean
    protected ConnectivityHelper connectivityHelper;

    public ResumeSyncableBaseListFragment(Class<E> clazz) {
        super(clazz);
    }

    @Override
    protected void init() {
        super.init();
        if (isSingle()) {
            presenter.getDisposables().add(rxStore.isSyncInProgressObservable(account)
                    .skip(1) // do not refresh current state
                    .filter(syncStatus -> !syncStatus.isInProgress())
                    .subscribeOn(Schedulers.computation())
                    .subscribe(syncStatus -> onRefresh()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!synced && isResumeSyncable()) {
            synced = true;
            if (connectivityHelper.isNetworkAvailable()) {
                onRefresh();
            }
        }
    }

    // override for customizable behaviour of fragment
    public boolean isResumeSyncable() {
        return true;
    }
}

