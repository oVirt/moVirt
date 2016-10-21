package org.ovirt.mobile.movirt.ui.vms;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.ConsoleFacade;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.facade.SnapshotFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Console;
import org.ovirt.mobile.movirt.model.ConsoleProtocol;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.util.MemorySize;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

@EFragment(R.layout.fragment_vm_detail_general)
public class VmDetailGeneralFragment extends RefreshableLoaderFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = VmDetailGeneralFragment.class.getSimpleName();
    private static final int VMS_LOADER = 0;
    private static final int CLUSTER_LOADER = 1;
    private static final int DATA_CENTER_LOADER = 2;
    private static final int HOST_LOADER = 3;
    private static final int CONSOLES_LOADER = 4;

    private static final String VM_URI = "vm_uri";

    private String vmId = null;

    @ViewById
    TextView statusView;

    @ViewById
    TextView cpuView;

    @ViewById
    TextView memView;

    @ViewById
    TextView memoryView;

    @ViewById
    TextView socketView;

    @ViewById
    TextView coreView;

    @ViewById
    TextView osView;

    @ViewById
    TextView displayView;

    @ViewById
    TextView clusterView;

    @ViewById
    TextView dataCenterView;

    @ViewById
    Button hostButton;

    Bundle args;

    @Bean
    ProviderFacade provider;

    @Bean
    VmFacade vmFacade;

    @Bean
    HostFacade hostFacade;

    @Bean
    SnapshotFacade snapshotFacade;

    @Bean
    ConsoleFacade consoleFacade;

    Vm vm;

    Host host;

    Cluster cluster;

    DataCenter dataCenter;

    @ViewById
    SwipeRefreshLayout swipeGeneralContainer;

    @AfterViews
    void initLoader() {
        hideProgressBar();
        Uri vmUri = getActivity().getIntent().getData();

        args = new Bundle();
        args.putParcelable(VM_URI, vmUri);
        getLoaderManager().initLoader(VMS_LOADER, args, this);
        vmId = vmUri.getLastPathSegment();
        renderDisplayView("");
        getLoaderManager().initLoader(CONSOLES_LOADER, args, this);
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(VMS_LOADER, args, this);
        getLoaderManager().restartLoader(CONSOLES_LOADER, args, this);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(VMS_LOADER);
        getLoaderManager().destroyLoader(CLUSTER_LOADER);
        getLoaderManager().destroyLoader(DATA_CENTER_LOADER);
        getLoaderManager().destroyLoader(HOST_LOADER);
        getLoaderManager().destroyLoader(CONSOLES_LOADER);
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeGeneralContainer;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;

        switch (id) {
            case VMS_LOADER:
                String vmId = args.<Uri>getParcelable(VM_URI).getLastPathSegment();
                loader = provider.query(Vm.class).id(vmId).asLoader();
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
            case HOST_LOADER:
                if (vm != null) {
                    loader = provider.query(Host.class).id(vm.getHostId()).asLoader();
                }
                break;
            case CONSOLES_LOADER:
                vmId = args.<Uri>getParcelable(VM_URI).getLastPathSegment();
                loader = provider.query(Console.class).where(OVirtContract.Console.VM_ID, vmId).asLoader();
                break;
            default:
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            if (loader.getId() == HOST_LOADER) {
                host = null;
                renderHost(host);
            } else {
                Log.e(TAG, "Error loading data: id=" + loader.getId());
            }
            return;
        }

        switch (loader.getId()) {
            case VMS_LOADER:
                vm = vmFacade.mapFromCursor(data);
                renderVm(vm);
                if (getLoaderManager().getLoader(CLUSTER_LOADER) == null) {
                    getLoaderManager().initLoader(CLUSTER_LOADER, null, this);
                }

                if (getLoaderManager().getLoader(HOST_LOADER) != null) {
                    getLoaderManager().restartLoader(HOST_LOADER, null, this);
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
                dataCenter = EntityMapper.forEntity(DataCenter.class).fromCursor(data);
                renderDataCenter(dataCenter);
                if (getLoaderManager().getLoader(HOST_LOADER) == null) {
                    getLoaderManager().initLoader(HOST_LOADER, null, this);
                }
                break;
            case HOST_LOADER:
                host = hostFacade.mapFromCursor(data);
                renderHost(host);
                break;
            case CONSOLES_LOADER: // no consoles for snapshots, because they have different id
                SortedSet<ConsoleProtocol> protocols = ConsoleProtocol.getProtocolTypes(consoleFacade.mapAllFromCursor(data));
                Iterator<ConsoleProtocol> it = protocols.iterator();
                String displayTypes = "";

                while (it.hasNext()) {
                    displayTypes += it.next();
                    if (it.hasNext()) {
                        displayTypes += " + ";
                    }
                }
                renderDisplayView(displayTypes);
                break;
            default:
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    public void renderVm(Vm vm) {
        statusView.setText(vm.getStatus().toString().toLowerCase());
        cpuView.setText(getString(R.string.percentage, vm.getCpuUsage()));
        memView.setText(getString(R.string.percentage, vm.getMemoryUsage()));
        long memory = vm.getMemorySize();
        memoryView.setText((memory == -1) ? getString(R.string.NA) : new MemorySize(memory).toString());
        socketView.setText(String.valueOf(vm.getSockets()));
        coreView.setText(String.valueOf(vm.getCoresPerSocket()));
        osView.setText(vm.getOsType());
    }

    public void renderDisplayView(String displayTypes) {
        displayView.setText(StringUtils.isEmpty(displayTypes) ? getString(R.string.NA) : displayTypes);
    }

    public void renderHost(Host host) {
        if (host != null) {
            hostButton.setText(host.getName());
            hostButton.setEnabled(true);
        } else {
            hostButton.setText(getString(R.string.NA));
            hostButton.setEnabled(false);
        }
    }

    public void renderCluster(Cluster cluster) {
        clusterView.setText(getString(R.string.two_separated_strings, cluster.getName(), cluster.getVersion()));
    }

    public void renderDataCenter(DataCenter dataCenter) {
        dataCenterView.setText(getString(R.string.two_separated_strings, dataCenter.getName(), dataCenter.getVersion()));
    }

    @Click(R.id.hostButton)
    void btnHost() {
        if (host != null) {
            startActivity(hostFacade.getDetailIntent(host, getActivity()));
        }
    }

    @Override
    @Background
    public void onRefresh() {
        // a hack because of https://bugzilla.redhat.com/1348138 - e.g. if we don't know the
        // type we don't know if to show this details. But better than completely disable this feature it
        // is better to show the tab always and just don't fail on NPE here.
        if (vm == null) {
            hideProgressBar();
            return;
        }
        if (vm.isSnapshotEmbedded()) {
            String snapshotId = vm.getSnapshotId();
            String vmId = provider.query(Snapshot.class).id(snapshotId).first().getVmId();
            snapshotFacade.syncOne(new ProgressBarResponse<Snapshot>(this), snapshotId, vmId);
        } else {
            vmFacade.syncOne(new ProgressBarResponse<Vm>(this), vmId);
            consoleFacade.syncAll(new ProgressBarResponse<List<Console>>(this), vmId);
        }
    }
}
