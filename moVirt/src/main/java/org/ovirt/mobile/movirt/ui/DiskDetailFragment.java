package org.ovirt.mobile.movirt.ui;

import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.rest.Disks;
import org.ovirt.mobile.movirt.rest.OVirtClient;

@EFragment(R.layout.fragment_disk_detail)
public class DiskDetailFragment extends RefreshableFragment {
    private static final String TAG = DiskDetailFragment.class.getSimpleName();

    @ViewById(R.id.diskListView)
    ListView listView;

    @Bean
    OVirtClient oVirtClient;

    DiskListAdapter diskListAdapter;

    @InstanceState
    String vmId = "";

    @ViewById
    SwipeRefreshLayout swipeDisksContainer;

    @Override
    public void onResume() {
        super.onResume();
        loadDiskDetails();
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeDisksContainer;
    }

    @UiThread
    void displayListView(Disks disks) {
        if (listView != null && disks != null) {
            diskListAdapter = new DiskListAdapter(getActivity(), 0, disks);
            listView.setAdapter(diskListAdapter);
        }
    }

    @Background
    void loadDiskDetails() {
        oVirtClient.getDisks(vmId, new ProgressBarResponse<Disks>(this) {
            @Override
            public void onResponse(Disks disks) throws RemoteException {
                displayListView(disks);
            }
        });

    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public void onRefresh() {
        loadDiskDetails();
    }
}
