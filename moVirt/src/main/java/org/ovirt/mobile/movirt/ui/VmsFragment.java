package org.ovirt.mobile.movirt.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.sync.SyncUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.NamedEntity.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.CLUSTER_ID;

@EFragment(R.layout.fragment_vms_list)
public class VmsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = VmsFragment.class.getSimpleName();

    private static final int EVENTS_PER_PAGE = 20;

    private SimpleCursorAdapter vmListAdapter;

    private int page = 1;

    @ViewById
    ProgressBar vmsProgress;

    @Bean
    SyncAdapter syncAdapter;

    @ViewById(R.id.vmListView)
    ListView listView;

    @ViewById
    EditText searchText;

    @ViewById
    Spinner orderBySpinner;

    @ViewById
    Spinner orderSpinner;

    @InstanceState
    String selectedClusterId;

    @Bean
    ProviderFacade provider;

    @ViewById
    SwipeRefreshLayout swipeVmContainer;

    private final TextWatcher textWatcher = new TextWatcher() {
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
    };

    private final EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadMoreData(page);
        }
    };

    public void loadMoreData(int page) {
        this.page = page;
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        syncingChanged(SyncAdapter.inSync);
        performRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        syncingChanged(false);
    }

    @AfterViews
    void initAdapters() {
        swipeVmContainer.setOnRefreshListener(this);

        vmListAdapter = new SimpleCursorAdapter(getActivity(),
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
        listView.setEmptyView(getActivity().findViewById(android.R.id.empty));
        listView.setTextFilterEnabled(true);

        getLoaderManager().initLoader(0, null, this);

        listView.setOnScrollListener(endlessScrollListener);

        searchText.removeTextChangedListener(textWatcher);
        searchText.addTextChangedListener(textWatcher);

        RestartOrderItemSelectedListener orderItemSelectedListener = new RestartOrderItemSelectedListener();

        orderBySpinner.setOnItemSelectedListener(orderItemSelectedListener);
        orderSpinner.setOnItemSelectedListener(orderItemSelectedListener);
    }

    private void resetListViewPosition() {
        page = 1;
        listView.smoothScrollToPosition(0);
        endlessScrollListener.resetListener();
    }

    public void updateFilterClusterIdTo(String selectedClusterId) {
        resetListViewPosition();
        this.selectedClusterId = selectedClusterId;
        restartLoader();
    }

    @Override
    public void onRefresh() {
        performRefresh();
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
    public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        if(vmListAdapter != null && cursor != null) {
            vmListAdapter.swapCursor(cursor); //swap the new cursor in.
        }
        else {
            Log.v(TAG, "OnLoadFinished: vmListAdapter is null");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(TAG, "onLoaderReset 1");
        if(vmListAdapter!=null) {
            Log.v(TAG, "onLoaderReset 2");
            vmListAdapter.swapCursor(null);
        }
        else {
            Log.v(TAG, "OnLoadFinished: vmListAdapter is null");
        }
    }

    @ItemClick
    void vmListViewItemClicked(Cursor cursor) {
        Intent intent = new Intent(getActivity(), VmDetailActivity_.class);
        Vm vm = EntityMapper.VM_MAPPER.fromCursor(cursor);
        intent.setData(vm.getUri());
        startActivity(intent);
    }

    @UiThread
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        vmsProgress.setVisibility(syncing ? View.VISIBLE : View.GONE);
        swipeVmContainer.setRefreshing(syncing);
    }

    @Background
    void performRefresh() {
        syncAdapter.doPerformSync(false);
    }
}
