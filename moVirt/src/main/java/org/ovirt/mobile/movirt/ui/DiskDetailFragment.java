package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.rest.Disks;
import org.ovirt.mobile.movirt.rest.OVirtClient;

@EFragment(R.layout.fragment_disk_detail)
public class DiskDetailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
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
    void showProgressBar() {
        diskProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    void hideProgressBar() {
        diskProgress.setVisibility(View.GONE);
    }

    @UiThread
    void displayListView(Disks disks) {
        diskListAdapter = new DiskListAdapter(getActivity(), 0, disks);
        listView.setAdapter(diskListAdapter);
    }

    @Background
    void loadDiskDetails() {
        oVirtClient.getDisks(vmId, new OVirtClient.SimpleResponse<Disks>() {

            @Override
            public void before() {
                showProgressBar();
            }

            @Override
            public void onResponse(Disks disks) throws RemoteException {
                displayListView(disks);
            }

            @Override
            public void after() {
                hideProgressBar();
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
