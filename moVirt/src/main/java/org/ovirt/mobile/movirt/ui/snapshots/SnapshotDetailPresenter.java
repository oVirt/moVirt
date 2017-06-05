package org.ovirt.mobile.movirt.ui.snapshots;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.util.Pair;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.ClusterAndEntity;
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.facade.SnapshotFacade;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.SnapshotStatus;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.rest.dto.SnapshotAction;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.mvp.AccountDisposablesProgressBarPresenter;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@EBean
public class SnapshotDetailPresenter extends AccountDisposablesProgressBarPresenter<SnapshotDetailPresenter, SnapshotDetailContract.View>
        implements SnapshotDetailContract.Presenter {

    private final Subject<MenuState> menuState = PublishSubject.<MenuState>create().toSerialized();

    private String vmId;
    private String snapshotId;

    private Snapshot snapshot;

    @Bean
    ProviderFacade providerFacade;

    @RootContext
    Context context;

    @Bean
    EnvironmentStore environmentStore;

    @Bean
    CommonMessageHelper commonMessageHelper;

    @Override
    public SnapshotDetailPresenter setIds(String snapshotId, String vmId) {
        this.snapshotId = snapshotId;
        this.vmId = vmId;
        return this;
    }

    @Override
    public SnapshotDetailPresenter initialize() {
        super.initialize();
        ObjectUtils.requireAllNotNull(snapshotId, vmId);

        final Observable<Vm> vmObservable = providerFacade.query(Vm.class)
                .id(vmId)
                .singleAsObservable();

        final Observable<Snapshot> snapshotObservable = providerFacade.query(Snapshot.class)
                .id(snapshotId)
                .singleAsObservable();

        getDisposables().add(Observable.combineLatest(vmObservable, snapshotObservable, Pair::new)
                .switchMap(pair -> providerFacade.query(Cluster.class)
                        .where(Cluster.ID, pair.first.getClusterId())
                        .singleAsObservable()
                        .map(cluster -> new ClusterWrapper(pair.first, cluster, pair.second))
                        .startWith(new ClusterWrapper(pair.first, null, pair.second)))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wrapper -> {
                    getView().displayStatus(new Selection(account, wrapper.getClusterName(),
                            wrapper.entity.getName(), wrapper.currentSnapshot.getName()));
                }));

        getDisposables().add(snapshotObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(snapshot -> {
                    getView().displayTitle(snapshot.getName());
                    this.snapshot = snapshot;
                }));

        getDisposables().add(Observable.combineLatest(
                vmObservable, snapshotObservable,
                providerFacade.query(Snapshot.class).where(Snapshot.VM_ID, vmId).asObservable(), Wrapper::new)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wrapper -> {
                    boolean allSnapshotsOk = !Snapshot.containsOneOfStatuses(wrapper.snapshots, SnapshotStatus.LOCKED, SnapshotStatus.IN_PREVIEW);
                    menuState.onNext(new MenuState(wrapper.vm.getStatus(),
                            wrapper.currentSnapshot.getSnapshotStatus(), allSnapshotsOk));
                }));

        getDisposables().add(menuState.distinctUntilChanged()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> getView().displayMenu(state)));

        getDisposables().add(rxStore.isSyncInProgressObservable(account)
                .skip(1) // do not refresh current state
                .filter(syncStatus -> !syncStatus.isInProgress())
                .subscribeOn(Schedulers.io())
                .subscribe(syncStatus -> syncSnapshots()));

        return this;
    }

    @Override
    public void onPreviewSnapshot() {
        if (snapshot.getPersistMemorystate()) {
            getView().openPreviewDialog();
        } else {
            previewSnapshot(false);
        }
    }

    @Override
    public void onRestoreSnapshot() {
        if (snapshot.getPersistMemorystate()) {
            getView().openRestoreDialog();
        } else {
            restoreSnapshot(false);
        }
    }

    @Background
    @Override
    public void previewSnapshot(boolean restoreMemory) {
        SnapshotAction action = new SnapshotAction(snapshotId, restoreMemory);
        environmentStore.safeOvirtClientCall(account,
                client -> client.previewSnapshot(action, vmId, new SyncSnapshotsResponse()));
    }

    @Background
    @Override
    public void restoreSnapshot(boolean restoreMemory) {
        SnapshotAction action = new SnapshotAction(snapshotId, restoreMemory);
        environmentStore.safeOvirtClientCall(account,
                client -> client.restoreSnapshot(action, vmId, new SyncSnapshotsResponse()));
    }

    @Background
    @Override
    public void commitSnapshot() {
        environmentStore.safeOvirtClientCall(account,
                client -> client.commitSnapshot(vmId, new SyncSnapshotsResponse()));
    }

    @Background
    @Override
    public void undoSnapshot() {
        environmentStore.safeOvirtClientCall(account,
                client -> client.undoSnapshot(vmId, new SyncSnapshotsResponse()));
    }

    private void syncSnapshots() {
        try {
            SnapshotFacade facade = environmentStore.getEnvironment(account).getFacade(Snapshot.class);
            facade.syncAll(new ProgressBarResponse<>(this), vmId);
        } catch (AccountDeletedException ignore) {
        }
    }

    @Override
    @Background
    public void deleteSnapshot() {
        environmentStore.safeOvirtClientCall(account,
                client -> client.deleteSnapshot(vmId, snapshotId, new SimpleResponse<Void>() {
                    @Override
                    public void onResponse(Void aVoid) throws RemoteException {
                        finishSafe();
                        syncSnapshots();
                    }
                }));
    }

    @Override
    public void destroy() {
        super.destroy();
        menuState.onComplete();
    }

    private class SyncSnapshotsResponse extends SimpleResponse<Void> {
        @Override
        public void onResponse(Void aVoid) throws RemoteException {
            syncSnapshots();
        }
    }

    private static class ClusterWrapper extends ClusterAndEntity<Vm> {
        final Snapshot currentSnapshot;

        public ClusterWrapper(Vm entity, Cluster cluster, Snapshot currentSnapshot) {
            super(entity, cluster);
            this.currentSnapshot = currentSnapshot;
        }
    }

    private static class Wrapper {
        final Vm vm;
        final Snapshot currentSnapshot;
        final List<Snapshot> snapshots;

        Wrapper(Vm vm, Snapshot currentSnapshot, List<Snapshot> snapshots) {
            this.vm = vm;
            this.currentSnapshot = currentSnapshot;
            this.snapshots = snapshots;
        }
    }
}
