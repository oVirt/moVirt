package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.*;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.sync.SyncUtils;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SELECT_CLUSTER_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String[] PROJECTION = new String[]{OVirtContract.Vm.NAME, OVirtContract.Vm._ID};

    @App
    MoVirtApp app;

    @ViewById(R.id.vmListView)
    ListView listView;

    @ViewById
    Button selectCluster;

    private SimpleCursorAdapter vmListAdapter;
    private String selectedClusterId;
    private String selectedClusterName;

    @AfterViews
    void initAdapters() {
        if (!app.endpointConfigured()) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.settings_dialog);
            dialog.setTitle(getString(R.string.configuration));
            Button continueButton = (Button) dialog.findViewById(R.id.continueButton);
            continueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    showSettings();
                }
            });
            dialog.show();
            return;
        }

        vmListAdapter = new SimpleCursorAdapter(this,
                                                R.layout.vm_list_item,
                                                null,
                                                PROJECTION,
                                                new int[] {R.id.vm_view});

        vmListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                TextView textView = (TextView) view;
                String vmName = cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.NAME));
                textView.setText(vmName);

                return true;
            }
        });
        listView.setAdapter(vmListAdapter);
        listView.setEmptyView(findViewById(android.R.id.empty));
        listView.setTextFilterEnabled(true);

        updateSelectedCluster(null, null);

    }

    @Click
    void refresh() {
        Log.d(TAG, "Refresh button clicked");

        SyncUtils.triggerRefresh();
    }

    @UiThread
    void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Click
    void selectCluster() {
        startActivityForResult(new Intent(this, SelectClusterActivity_.class), SELECT_CLUSTER_CODE);
    }

    @Click
    void editTriggers() {
        final Intent intent = new Intent(this, EditTriggersActivity_.class);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_ID, selectedClusterId);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_NAME, selectedClusterName);
        startActivity(intent);
    }

    @OptionsItem(R.id.action_settings)
    void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_CLUSTER_CODE:
                if (resultCode == RESULT_OK) {
                    updateSelectedCluster(data.getStringExtra(SelectClusterActivity.EXTRA_CLUSTER_ID),
                                          data.getStringExtra(SelectClusterActivity.EXTRA_CLUSTER_NAME));
                }
                break;
        }
    }

    private void updateSelectedCluster(String clusterId, String clusterName) {
        Log.d(TAG, "Updating selected cluster: id=" + clusterId + ", name=" + clusterName);
        selectCluster.setText(clusterName == null ? getString(R.string.all_clusters) : clusterName);
        selectedClusterId = clusterId;
        selectedClusterName = clusterName;
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                                OVirtContract.Vm.CONTENT_URI,
                                PROJECTION,
                                getClusterSelection(),
                                getClusterSelectionArgs(),
                                OVirtContract.Vm.NAME + " asc");
    }

    private String getClusterSelection() {
        if (selectedClusterId == null) {
            return null;
        }
        return OVirtContract.Vm.CLUSTER_ID + " = ?";
    }

    private String[] getClusterSelectionArgs() {
        if (selectedClusterId == null) {
            return null;
        }
        return new String[] {selectedClusterId};
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        vmListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        vmListAdapter.swapCursor(null);
    }
}
