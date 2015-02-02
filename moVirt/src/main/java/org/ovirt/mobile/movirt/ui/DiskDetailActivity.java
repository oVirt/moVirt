package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;

/**
 * Created by sphoorti on 30/1/15.
 */
@EActivity(R.layout.activity_disk_detail)
public class DiskDetailActivity extends Activity {

    @ViewById(R.id.diskListView)
    ListView listView;

    @AfterViews
    void initLoader() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
