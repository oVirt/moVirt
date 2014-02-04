package org.ovirt.mobile.movirt.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.R;

@EActivity(R.layout.activity_cluster)
public class SelectClusterActivity extends ListActivity {

    private static final String TAG = SelectClusterActivity.class.getSimpleName();
    private ClusterListAdapter clusterListAdapter;

    @AfterViews
    void initListView() {
        clusterListAdapter = new ClusterListAdapter(this);
        setListAdapter(clusterListAdapter);

        refresh();
    }

    @Background
    void refresh() {
        try {
//            clusterListAdapter.fetchData();
            updateClusters();
        } catch (Exception e) {
            showError(e.getMessage());
        }

    }

    @UiThread
    void updateClusters() {
        clusterListAdapter.notifyDataSetChanged();
    }

    @UiThread
    void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @ItemClick(android.R.id.list)
    void selectClusterItemClicked(String cluster) {
        Log.i(TAG, "Cluster " + (cluster == null ? "<ALL>" : cluster) + " selected");
        Intent intent = getIntent();
        intent.putExtra("cluster", cluster);
        setResult(RESULT_OK, intent);
        finish();
    }
    
}
