package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.Disk;
import org.ovirt.mobile.movirt.rest.Disks;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    List <Disk> disks;

    DiskListAdapter diskListAdapter;

    public static final String FILTER_VM_ID = "vmId";

    @AfterViews
    void init() {
        //Log.i(TAG, "SPH: value " + getIntent().getStringExtra(FILTER_VM_ID));
        showProgressBar();
        getDiskDetails();
        hideProgressBar();
        //displayListView();

        //listView.setFilterText("hello" + getIntent().getStringExtra(FILTER_VM_ID) );//+ disks.disk.size());
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

    @UiThread
    void displayListView() {
        //diskList.add(disks.disk.get(0).name + disks.disk.get(0).size);
        diskListAdapter = new DiskListAdapter(DiskDetailActivity.this,0,disks);
        listView.setAdapter(diskListAdapter);
    }

    @Background
    void getDiskDetails() {
        Log.i(TAG, "SPH: value " + getIntent().getStringExtra(FILTER_VM_ID));
        disks = oVirtClient.getDiskData(getIntent().getStringExtra(FILTER_VM_ID));
        Log.i(TAG, "SPH: disks " + disks.size());
        displayListView();
    }

}
