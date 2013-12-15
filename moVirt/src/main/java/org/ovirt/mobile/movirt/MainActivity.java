package org.ovirt.mobile.movirt;

import android.app.Dialog;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import java.io.IOException;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity implements uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener {

    private static final int SELECT_CLUSTER_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    @App
    MovirtApp app;

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

        vmListAdapter = new VmListAdapter(app.getClient());
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
    void refresh() {
        try {
            vmListAdapter.fetchData();
            updateVms();
        } catch (Exception e) {
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
