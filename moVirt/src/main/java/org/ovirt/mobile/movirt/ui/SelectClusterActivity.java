package org.ovirt.mobile.movirt.ui;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.res.TextRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.OVirtContract;

@EActivity(R.layout.activity_cluster)
public class SelectClusterActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = SelectClusterActivity.class.getSimpleName();
    private SimpleCursorAdapter clusterListAdapter;
    private MatrixCursor emptyClusterCursor;

    @StringRes(R.string.all_clusters)
    String allClusters;

    private static final String[] PROJECTION = new String[] {OVirtContract.Cluster.NAME, OVirtContract.Cluster._ID};

    @AfterViews
    void initListView() {
        emptyClusterCursor = new MatrixCursor(PROJECTION);
        emptyClusterCursor.addRow(new String[] {allClusters, null});


        clusterListAdapter = new SimpleCursorAdapter(this,
                                                     R.layout.cluster_list_item,
                                                     null,
                                                     PROJECTION,
                                                     new int[] {R.id.cluster_view});
        clusterListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex != 0) {
                    // do not display id column
                    return true;
                }
                TextView textView = (TextView) view;
                String clusterName = cursor.getString(cursor.getColumnIndex(OVirtContract.Cluster.NAME)); // only names selected, thus only 1 column
                textView.setText(clusterName);

                return true;
            }
        });
        setListAdapter(clusterListAdapter);

        getLoaderManager().initLoader(0, null, this);

    }

    @UiThread
    void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @ItemClick(android.R.id.list)
    void selectClusterItemClicked(Cursor cursor) {
        String clusterId = cursor.getString(cursor.getColumnIndex(OVirtContract.Cluster._ID));
        Log.i(TAG, "Cluster " + (clusterId == null ? "<ALL>" : clusterId) + " selected");
        Intent intent = getIntent();
        intent.putExtra("cluster", clusterId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                                OVirtContract.Cluster.CONTENT_URI,
                                PROJECTION,
                                null,
                                null,
                                OVirtContract.Cluster.NAME + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        clusterListAdapter.swapCursor(new MergeCursor(new Cursor[] { emptyClusterCursor, data}));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        clusterListAdapter.swapCursor(null);
    }
}
