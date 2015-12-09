package org.ovirt.mobile.movirt.ui.vms;

/**
 * Created by yixin on 2015/3/24.
 */

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
import org.ovirt.mobile.movirt.rest.Nics;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableFragment;

@EFragment(R.layout.fragment_nic_detail)
public class VmNicDetailFragment extends RefreshableFragment {
    private static final String TAG = VmNicDetailFragment.class.getSimpleName();

    @ViewById(R.id.nicListView)
    ListView listView;

    @Bean
    OVirtClient oVirtClient;

    VmNicListAdapter vmNicListAdapter;

    @InstanceState
    String vmId = "";

    @ViewById
    SwipeRefreshLayout swipeNicsContainer;

    @Override
    public void onResume() {
        super.onResume();
        loadNicDetails();
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeNicsContainer;
    }

    @UiThread
    void displayListView(Nics nics) {
        if (listView != null && nics != null && nics.nic != null) {
            vmNicListAdapter = new VmNicListAdapter(getActivity(), 0, nics);
            listView.setAdapter(vmNicListAdapter);
        }
    }

    @Background
    void loadNicDetails() {
        oVirtClient.getNics(vmId, new ProgressBarResponse<Nics>(this) {
            @Override
            public void onResponse(Nics nics) throws RemoteException {
                displayListView(nics);
            }
        });
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public void onRefresh() {
        loadNicDetails();
    }
}
