package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
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
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.*;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.*;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity {

    private static final int SELECT_CLUSTER_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SELECTED_CLUSTER_ID = "selected_cluster_id";
    private static final String SELECTED_CLUSTER_NAME = "selected_cluster_name";

    @App
    MoVirtApp app;

    @ViewById
    Button selectCluster;

    @ViewById(R.id.vmListView)
    ListView listView;

    @FragmentById
    EventsFragment eventList;

    @Bean
    ProviderFacade provider;

    @InstanceState
    String selectedClusterId;

    @InstanceState
    String selectedClusterName;

    private CursorAdapterLoader cursorAdapterLoader;

    private final BroadcastReceiver connectionStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MoVirtApp.CONNECTION_SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.connected, Toast.LENGTH_LONG).show();
                    break;
                case MoVirtApp.CONNECTION_FAILURE:
                    Toast.makeText(MainActivity.this, R.string.disconnected, Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MoVirtApp.CONNECTION_SUCCESS);
        intentFilter.addAction(MoVirtApp.CONNECTION_FAILURE);
        registerReceiver(connectionStatusReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(connectionStatusReceiver);
    }

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

        SimpleCursorAdapter vmListAdapter = new SimpleCursorAdapter(this,
                                                                    R.layout.vm_list_item,
                                                                    null,
                                                                    new String[]{OVirtContract.Vm.NAME, OVirtContract.Vm.STATUS, OVirtContract.Vm.MEMORY_USAGE, OVirtContract.Vm.CPU_USAGE},
                                                                    new int[]{R.id.vm_name, R.id.vm_status, R.id.vm_memory, R.id.vm_cpu});

        cursorAdapterLoader = new CursorAdapterLoader(vmListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                ProviderFacade.QueryBuilder<Vm> query = provider.query(Vm.class);
                if (selectedClusterId != null) {
                    query.where(CLUSTER_ID, selectedClusterId);
                }
                return query.orderBy(NAME).asLoader();
            }
        };

        vmListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(OVirtContract.Vm.NAME)) {
                    TextView textView = (TextView) view;
                    String vmName = cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.NAME));
                    textView.setText(vmName);
                } else if (columnIndex == cursor.getColumnIndex(OVirtContract.Vm.STATUS)) {
                    TextView textView = (TextView) view;
                    String vmStatus = cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.STATUS));
                    textView.setText(vmStatus);
                } else if (columnIndex == cursor.getColumnIndex(OVirtContract.Vm.MEMORY_USAGE)) {
                    TextView textView = (TextView) view;
                    double vmMem = cursor.getDouble(cursor.getColumnIndex(OVirtContract.Vm.MEMORY_USAGE));
                    textView.setText(String.format("Mem: %.2f%%", vmMem));
                } else if (columnIndex == cursor.getColumnIndex(OVirtContract.Vm.CPU_USAGE)) {
                    TextView textView = (TextView) view;
                    double vmCpu = cursor.getDouble(cursor.getColumnIndex(OVirtContract.Vm.CPU_USAGE));
                    textView.setText(String.format("CPU: %.2f%%", vmCpu));
                }

                return true;
            }
        });
        listView.setAdapter(vmListAdapter);
        listView.setEmptyView(findViewById(android.R.id.empty));
        listView.setTextFilterEnabled(true);

        updateSelectedCluster(selectedClusterId, selectedClusterName);
    }

    @OptionsItem(R.id.action_refresh)
    void refresh() {
        Log.d(TAG, "Refresh button clicked");

        SyncUtils.triggerRefresh();
    }

    @OptionsItem(R.id.action_settings)
    void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
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
        intent.putExtra(EditTriggersActivity.EXTRA_SCOPE, selectedClusterId == null ? Trigger.Scope.GLOBAL : Trigger.Scope.CLUSTER);
        startActivity(intent);
    }

    @ItemClick
    void vmListViewItemClicked(Cursor cursor) {
        Intent intent = new Intent(this, VmDetailActivity_.class);
        Vm vm = EntityMapper.VM_MAPPER.fromCursor(cursor);
        intent.setData(vm.getUri());
        startActivity(intent);
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
        selectCluster.setText(clusterName == null ? getString(R.string.whole_datacenter) : clusterName);
        selectedClusterId = clusterId;
        selectedClusterName = clusterName;
        getLoaderManager().restartLoader(0, null, cursorAdapterLoader);
        eventList.setFilterClusterId(selectedClusterId);
    }
}
