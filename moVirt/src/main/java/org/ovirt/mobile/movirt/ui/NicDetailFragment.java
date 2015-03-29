package org.ovirt.mobile.movirt.ui;

/**
 * Created by yixin on 2015/3/24.
 */
import android.support.v4.app.Fragment;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.rest.Nics;
import org.ovirt.mobile.movirt.rest.OVirtClient;

@EFragment(R.layout.fragment_nic_detail)
public class NicDetailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, HasProgressBar {
    private static final String TAG = NicDetailFragment.class.getSimpleName();

    @ViewById(R.id.nicListView)
    ListView listView;

    @Bean
    OVirtClient oVirtClient;

    NicListAdapter nicListAdapter;

    @InstanceState
    String vmId = "";

    @ViewById
    SwipeRefreshLayout swipeNicsContainer;

    @AfterViews
    void init() {
        swipeNicsContainer.setOnRefreshListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNicDetails();
    }

    @UiThread
    @Override
    public void showProgressBar() {
        swipeNicsContainer.setRefreshing(true);
    }

    @UiThread
    @Override
    public void hideProgressBar() {
        swipeNicsContainer.setRefreshing(false);
    }

    @UiThread
    void displayListView(Nics nics) {
        if (listView != null && nics != null && nics.nic != null) {
            nicListAdapter = new NicListAdapter(getActivity(), 0, nics);
            listView.setAdapter(nicListAdapter);
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
