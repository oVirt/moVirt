package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.rest.DiskData;
import org.ovirt.mobile.movirt.rest.Disks;
import org.ovirt.mobile.movirt.rest.ExtendedVm;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.List;

/**
 * Created by sphoorti on 30/1/15.
 */
@EActivity(R.layout.activity_disk_detail)
public class DiskDetailActivity extends Activity {

    @ViewById(R.id.diskListView)
    ListView listView;

    @Bean
    OVirtClient oVirtClient;

    @ViewById(R.id.diskProgress)
    ProgressBar diskProgress;

    Disks disks;

    public static final String FILTER_VM_ID = "vmId";

    @AfterViews
    void init() {
        showProgressBar();
        getDiskDetails();
        hideProgressBar();
        listView.setFilterText("hello" + getIntent().getStringExtra(FILTER_VM_ID) + disks.diskData.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @UiThread
    void showProgressBar() {
        diskProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    void hideProgressBar() {
        diskProgress.setVisibility(View.GONE);
    }

    @Background
    void getDiskDetails() {
        disks = oVirtClient.getDiskData(getIntent().getStringExtra(FILTER_VM_ID));
    }

}
