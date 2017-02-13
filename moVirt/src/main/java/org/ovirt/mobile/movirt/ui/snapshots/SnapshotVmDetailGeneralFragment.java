package org.ovirt.mobile.movirt.ui.snapshots;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.SnapshotFacade;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.SnapshotVm;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

@EFragment(R.layout.fragment_snapshot_vm_detail_general)
public class SnapshotVmDetailGeneralFragment extends RefreshableLoaderFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = SnapshotVmDetailGeneralFragment.class.getSimpleName();
    private static final int SNAPSHOT_VMS_LOADER = 0;
    private static final int CLUSTER_LOADER = 1;
    private static final int DATA_CENTER_LOADER = 2;

    @InstanceState
    protected String vmId;

    @InstanceState
    protected String snapshotId;

    @ViewById
    TextView statusView;

    @ViewById
    TextView memoryView;

    @ViewById
    TextView socketView;

    @ViewById
    TextView coreView;

    @ViewById
    TextView osView;

    @ViewById
    TextView clusterView;

    @ViewById
    TextView dataCenterView;

    @Bean
    ProviderFacade provider;

    @Bean
    SnapshotFacade snapshotFacade;

    private SnapshotVm vm;

    private Cluster cluster;

    @ViewById
    SwipeRefreshLayout swipeGeneralContainer;

    @AfterViews
    void initLoader() {
        hideProgressBar();
        getLoaderManager().initLoader(SNAPSHOT_VMS_LOADER, null, this);
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(SNAPSHOT_VMS_LOADER, null, this);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(SNAPSHOT_VMS_LOADER);
        getLoaderManager().destroyLoader(CLUSTER_LOADER);
        getLoaderManager().destroyLoader(DATA_CENTER_LOADER);
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeGeneralContainer;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;

        switch (id) {
            case SNAPSHOT_VMS_LOADER:
                loader = provider.query(SnapshotVm.class)
                        .where(OVirtContract.SnapshotVm.VM_ID, vmId)
                        .where(OVirtContract.SnapshotVm.SNAPSHOT_ID, snapshotId)
                        .asLoader();
                break;
            case CLUSTER_LOADER:
                if (vm != null) {
                    loader = provider.query(Cluster.class).id(vm.getClusterId()).asLoader();
                }
                break;
            case DATA_CENTER_LOADER:
                if (cluster != null) {
                    loader = provider.query(DataCenter.class).id(cluster.getDataCenterId()).asLoader();
                }
                break;
            default:
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading data: id=" + loader.getId());
            return;
        }

        switch (loader.getId()) {
            case SNAPSHOT_VMS_LOADER:
                vm = EntityMapper.forEntity(SnapshotVm.class).fromCursor(data);
                renderVm(vm);
                if (getLoaderManager().getLoader(CLUSTER_LOADER) == null) {
                    getLoaderManager().initLoader(CLUSTER_LOADER, null, this);
                }
                break;
            case CLUSTER_LOADER:
                cluster = EntityMapper.forEntity(Cluster.class).fromCursor(data);
                renderCluster(cluster);
                if (getLoaderManager().getLoader(DATA_CENTER_LOADER) == null) {
                    getLoaderManager().initLoader(DATA_CENTER_LOADER, null, this);
                }
                break;
            case DATA_CENTER_LOADER:
                DataCenter dataCenter = EntityMapper.forEntity(DataCenter.class).fromCursor(data);
                renderDataCenter(dataCenter);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public void renderVm(SnapshotVm vm) {
        statusView.setText(vm.getStatus().toString().toLowerCase());
        long memory = vm.getMemorySize();
        memoryView.setText((memory == -1) ? getString(R.string.NA) : new MemorySize(memory).toString());
        socketView.setText(String.valueOf(vm.getSockets()));
        coreView.setText(String.valueOf(vm.getCoresPerSocket()));
        osView.setText(vm.getOsType());
    }

    public void renderCluster(Cluster cluster) {
        clusterView.setText(getString(R.string.two_separated_strings, cluster.getName(), cluster.getVersion()));
    }

    public void renderDataCenter(DataCenter dataCenter) {
        dataCenterView.setText(getString(R.string.two_separated_strings, dataCenter.getName(), dataCenter.getVersion()));
    }

    @Override
    @Background
    public void onRefresh() {
        if (vmId != null && snapshotId != null) {
            snapshotFacade.syncOne(new ProgressBarResponse<Snapshot>(this), snapshotId, vmId);
        }
    }
}
