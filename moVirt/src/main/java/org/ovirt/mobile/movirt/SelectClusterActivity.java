package org.ovirt.mobile.movirt;

import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.UiThread;

@EActivity(R.layout.activity_cluster)
public class SelectClusterActivity extends ListActivity {

    private static final String TAG = SelectClusterActivity.class.getSimpleName();
    private ClusterListAdapter clusterListAdapter;

    @App
    MovirtApp app;

    @AfterViews
    void initListView() {
        clusterListAdapter = new ClusterListAdapter(app.getClient(), getString(R.string.all_clusters));
        setListAdapter(clusterListAdapter);

        refresh();
    }

    @Background
    void refresh() {
        try {
            clusterListAdapter.fetchData();
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
