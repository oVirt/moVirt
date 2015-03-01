package org.ovirt.mobile.movirt.ui;

import android.app.Fragment;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.rest.Disks;
import org.ovirt.mobile.movirt.rest.OVirtClient;

@EFragment(R.layout.fragment_disk_detail)
public class DiskDetailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, HasProgressBar {
    private static final String TAG = DiskDetailFragment.class.getSimpleName();

    @ViewById(R.id.diskListView)
    ListView listView;

    @Bean
    OVirtClient oVirtClient;

    @ViewById(R.id.diskProgress)
    ProgressBar diskProgress;

    DiskListAdapter diskListAdapter;

    String vmId = "";

    @ViewById
    SwipeRefreshLayout swipeDisksContainer;

    @AfterViews
    void init() {
        swipeDisksContainer.setOnRefreshListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressBar();
        loadDiskDetails();
    }

    @UiThread
    @Override
    public void showProgressBar() {
        diskProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    @Override
    public void hideProgressBar() {
        diskProgress.setVisibility(View.GONE);
    }

    @UiThread
    void displayListView(Disks disks) {
        diskListAdapter = new DiskListAdapter(getActivity(), 0, disks);
        listView.setAdapter(diskListAdapter);
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

        loadDiskDetails();
    }

    @Override
    public void onRefresh() {
        loadDiskDetails();
    }
}
