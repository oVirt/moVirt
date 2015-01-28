package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.os.RemoteException;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.rest.Disks;
import org.ovirt.mobile.movirt.rest.OVirtClient;

/**
 * Created by sphoorti on 30/1/15.
 */
@EActivity(R.layout.activity_disk_detail)
public class DiskDetailActivity extends Activity {
    private static final String TAG = DiskDetailActivity.class.getSimpleName();

    @ViewById(R.id.diskListView)
    ListView listView;

    @Bean
    OVirtClient oVirtClient;

    @ViewById(R.id.diskProgress)
    ProgressBar diskProgress;

    DiskListAdapter diskListAdapter;

    public static final String FILTER_VM_ID = "vmId";

    @AfterViews
    void init() {
        showProgressBar();
        getDiskDetails();
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
        diskListAdapter = new DiskListAdapter(DiskDetailActivity.this,0,disks);
        listView.setAdapter(diskListAdapter);
        hideProgressBar();
    }

    @Background
    void getDiskDetails() {
        oVirtClient.getDisks(getIntent().getStringExtra(FILTER_VM_ID), new OVirtClient.SimpleResponse<Disks>() {

            @Override
            public void onResponse(Disks disks) throws RemoteException {
                displayListView(disks);
            }

            @Override
            public void onError() {
                super.onError();
                hideProgressBar();
            }
        });

    }

}
