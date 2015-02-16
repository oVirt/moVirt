package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.ovirt.mobile.movirt.R;

/**
 * Created by sphoorti on 29/1/15.
 */
@EActivity(R.layout.activity_events)
public class EventsActivity extends Activity {
    public static final String FILTER_VM_ID = "vmId";

    @FragmentById
    EventsFragment eventList;

    @AfterViews
    void initLoader() {
          // eventList.setFilterVmId(FILTER_VM_ID);
        eventList.setFilterVmId(getIntent().getStringExtra(FILTER_VM_ID));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
