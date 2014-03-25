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
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

@EActivity(R.layout.activity_cluster)
public class SelectClusterActivity extends ListActivity {

    public static String EXTRA_CLUSTER_ID = "cluster_id";
    public static String EXTRA_CLUSTER_NAME = "cluster_name";

    private static final String TAG = SelectClusterActivity.class.getSimpleName();
    private SimpleCursorAdapter clusterListAdapter;
    private CursorAdapterLoader cursorAdapterLoader;
    private MatrixCursor emptyClusterCursor;

    @StringRes(R.string.all_clusters)
    String allClusters;

    private static final String[] PROJECTION = new String[] {OVirtContract.Cluster.NAME, OVirtContract.Cluster.ID};

    @AfterViews
    void initListView() {
        emptyClusterCursor = new MatrixCursor(PROJECTION);
        emptyClusterCursor.addRow(new String[] {allClusters, null});


        clusterListAdapter = new SimpleCursorAdapter(this,
                                                     R.layout.cluster_list_item,
                                                     null,
                                                     PROJECTION,
                                                     new int[] {R.id.cluster_view});

        cursorAdapterLoader = new CursorAdapterLoader(clusterListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(SelectClusterActivity.this,
                                        OVirtContract.Cluster.CONTENT_URI,
                                        null,
                                        null,
                                        null,
                                        OVirtContract.Cluster.NAME + " asc");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                super.onLoadFinished(loader, new MergeCursor(new Cursor[] { emptyClusterCursor, data}));
            }
        };

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

        getLoaderManager().initLoader(0, null, cursorAdapterLoader);

    }

    @UiThread
    void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @ItemClick(android.R.id.list)
    void selectClusterItemClicked(Cursor cursor) {
        final Cluster cluster = EntityMapper.CLUSTER_MAPPER.fromCursor(cursor);
        Log.i(TAG, "Cluster " + (cluster.getId() == null ? "<ALL>" : cluster.getId()) + " selected");
        Intent intent = getIntent();
        intent.putExtra(EXTRA_CLUSTER_ID, cluster.getId());
        intent.putExtra(EXTRA_CLUSTER_NAME, cluster.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
