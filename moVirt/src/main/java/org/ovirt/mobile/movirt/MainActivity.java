package org.ovirt.mobile.movirt;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity implements uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener {

    private static final int SELECT_CLUSTER_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Bean
    OVirtClient client;

    @ViewById(R.id.vmListView)
    ListView listView;

    @ViewById
    Button selectCluster;

    private VmListAdapter vmListAdapter;
    private PullToRefreshAttacher pullToRefreshAttacher;

    @Pref
    AppPrefs_ prefs;

    @AfterViews
    void initAdapters() {
        pullToRefreshAttacher = PullToRefreshAttacher.get(this);
        pullToRefreshAttacher.addRefreshableView(listView, this);

        if (!endpointConfigured()) {
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

        vmListAdapter = new VmListAdapter(client);
        listView.setAdapter(vmListAdapter);
        listView.setEmptyView(findViewById(android.R.id.empty));

        updateSelectedCluster(null);
    }

    private boolean endpointConfigured() {
        return prefs.endpoint().exists() &&
               prefs.username().exists() &&
               prefs.password().exists();
    }

    @Background
    @Click
    void refresh() {
        try {
            vmListAdapter.fetchData();
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
        pullToRefreshAttacher.setRefreshComplete();
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

        vmListAdapter.setClusterName(clusterName);
        refresh();
    }

    @Override
    public void onRefreshStarted(View view) {
        Log.i(TAG, "refresh started");
        refresh();
    }
}
