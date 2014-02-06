package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.*;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity {

    private static final int SELECT_CLUSTER_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    @App
    MoVirtApp app;

    @ViewById(R.id.vmListView)
    ListView listView;

    @ViewById
    Button selectCluster;

    private VmListAdapter vmListAdapter;

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
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            });
            dialog.show();
            return;
        }

        vmListAdapter = new VmListAdapter(this);
        listView.setAdapter(vmListAdapter);
        listView.setEmptyView(findViewById(android.R.id.empty));

        updateSelectedCluster(null);
    }

    @Background
    @Click
    void refresh() {
        try {
            //vmListAdapter.fetchData();
            updateVms();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            showError(e.getMessage());
        }
    }

    @UiThread
    void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @UiThread
    void updateVms() {
        vmListAdapter.notifyDataSetChanged();
    }

    @Click
    @OptionsItem(R.id.action_select_cluster)
    void selectCluster() {
        startActivityForResult(new Intent(this, SelectClusterActivity_.class), SELECT_CLUSTER_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_CLUSTER_CODE:
                if (resultCode == RESULT_OK) {
                    updateSelectedCluster(data.getStringExtra("cluster"));
                }
                break;
        }
    }

    private void updateSelectedCluster(String clusterName) {
        selectCluster.setText(clusterName == null ? getString(R.string.all_clusters) : clusterName);

     //   vmListAdapter.setClusterName(clusterName);
        refresh();
    }
}
