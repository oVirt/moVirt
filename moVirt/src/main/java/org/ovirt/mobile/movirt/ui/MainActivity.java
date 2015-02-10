package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.CLUSTER_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.NAME;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity implements ClusterDrawerFragment.ClusterSelectedListener,LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int page = 1;
    private static final int EVENTS_PER_PAGE = 20;
    private SimpleCursorAdapter vmListAdapter;

    @App
    MoVirtApp app;

    @ViewById
    DrawerLayout drawerLayout;

    @ViewById(R.id.vmListView)
    ListView listView;

    @ViewById
    Spinner orderBySpinner;

    @ViewById
    Spinner orderSpinner;

    @ViewById
    EditText searchText;

    @FragmentById
    EventsFragment eventList;

    @FragmentById
    ClusterDrawerFragment clusterDrawer;

    @StringRes(R.string.cluster_scope)
    String CLUSTER_SCOPE;

    @Bean
    ProviderFacade provider;

    @InstanceState
    String selectedClusterId;

    @InstanceState
    String selectedClusterName;

    @ViewById
    ProgressBar vmsProgress;

    private final EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadMoreData(page);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        syncingChanged(SyncAdapter.inSync);
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncingChanged(false);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        getActionBar().setTitle(title);
    }

    @AfterViews
    void initAdapters() {
        vmListAdapter = new SimpleCursorAdapter(this,
                                                                    R.layout.vm_list_item,
                                                                    null,
                                                                    new String[]{OVirtContract.Vm.NAME, OVirtContract.Vm.STATUS},
                                                                    new int[]{R.id.vm_name, R.id.vm_status});

        vmListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(OVirtContract.Vm.NAME)) {
                    TextView textView = (TextView) view;
                    String vmName = cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.NAME));
                    textView.setText(vmName);
                } else if (columnIndex == cursor.getColumnIndex(OVirtContract.Vm.STATUS)) {
                    ImageView imageView = (ImageView) view;
                    Vm.Status status = Vm.Status.valueOf(cursor.getString(cursor.getColumnIndex(OVirtContract.Vm.STATUS)));
                    imageView.setImageResource(status.getResource());
                }

                return true;
            }
        });
        listView.setAdapter(vmListAdapter);
        listView.setEmptyView(findViewById(android.R.id.empty));
        listView.setTextFilterEnabled(true);

        clusterDrawer.initDrawerLayout(drawerLayout);
        clusterDrawer.getDrawerToggle().syncState();

        onClusterSelected(new Cluster() {{ setId(selectedClusterId); setName(selectedClusterName); }});

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

        }

        getLoaderManager().initLoader(0, null, this);

        listView.setOnScrollListener(endlessScrollListener);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                resetListViewPosition();
                restartLoader();
            }
        });

        RestartOrderItemSelectedListener orderItemSelectedListener = new RestartOrderItemSelectedListener();

        orderBySpinner.setOnItemSelectedListener(orderItemSelectedListener);
        orderSpinner.setOnItemSelectedListener(orderItemSelectedListener);
    }

    class RestartOrderItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            resetListViewPosition();
            restartLoader();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        ProviderFacade.QueryBuilder<Vm> query = provider.query(Vm.class);
        if (selectedClusterId != null) {
            query.where(CLUSTER_ID, selectedClusterId);
        }

        String searchNameString = searchText.getText().toString();
        if (!"".equals(searchNameString)) {
            query.whereLike(NAME, "%" + searchNameString + "%");
        }

        String orderBy = (String) orderBySpinner.getSelectedItem();
        if ("".equals(orderBy)) {
            orderBy = NAME;
        }

        SortOrder order = SortOrder.from((String) orderSpinner.getSelectedItem());

        return query.orderBy(orderBy, order).limit(page * EVENTS_PER_PAGE).asLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor cursor) {
        if(vmListAdapter!=null && cursor!=null) {
            vmListAdapter.swapCursor(cursor); //swap the new cursor in.
        }
        else {
            Log.v(TAG, "OnLoadFinished: vmListAdapter is null");
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(vmListAdapter!=null) {
            vmListAdapter.swapCursor(null);
        }
        else {
            Log.v(TAG, "OnLoadFinished: vmListAdapter is null");
        }
    }

    public void loadMoreData(int page) {
        this.page = page;
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
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

    @OptionsItem(R.id.action_edit_triggers)
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (clusterDrawer.getDrawerToggle().onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClusterSelected(Cluster cluster) {
        Log.d(TAG, "Updating selected cluster: id=" + cluster.getId() + ", name=" + cluster.getName());
        setTitle(cluster.getId() == null ? getString(R.string.all_clusters) : String.format(CLUSTER_SCOPE, cluster.getName()));
        selectedClusterId = cluster.getId();
        selectedClusterName = cluster.getName();
        resetListViewPosition();

        eventList.updateFilterClusterIdTo(selectedClusterId);
        drawerLayout.closeDrawers();
        restartLoader();
    }

    private void resetListViewPosition() {
        page = 1;
        listView.setSelectionAfterHeaderView();
        endlessScrollListener.resetListener();
    }

    @UiThread
    @Receiver(actions = MoVirtApp.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void syncingChanged(@Receiver.Extra(MoVirtApp.SYNCING) boolean syncing) {
        vmsProgress.setVisibility(syncing ? View.VISIBLE : View.GONE);
    }

    @Receiver(actions = MoVirtApp.CONNECTION_FAILURE, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void connectionFailure(@Receiver.Extra(MoVirtApp.CONNECTION_FAILURE_REASON) String reason) {
        Toast.makeText(MainActivity.this, R.string.disconnected + " " + reason, Toast.LENGTH_LONG).show();
    }
}
