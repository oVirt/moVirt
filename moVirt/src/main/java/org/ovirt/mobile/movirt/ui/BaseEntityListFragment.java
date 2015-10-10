package org.ovirt.mobile.movirt.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.melnykov.fab.FloatingActionButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.EntityFacade;
import org.ovirt.mobile.movirt.facade.EntityFacadeLocator;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.HOST_ID;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class BaseEntityListFragment<E extends OVirtEntity> extends RefreshableFragment
        implements OVirtContract.HasCluster, OVirtContract.NamedEntity, SelectedClusterAware, HasLoader {

    private static final int ITEMS_PER_PAGE = 20;

    @Bean
    protected SyncUtils syncUtils;

    @InstanceState
    protected String filterHostId;

    @InstanceState
    protected String selectedClusterId;

    @ViewById(android.R.id.list)
    protected ListView listView;

    @ViewById
    protected EditText searchText;

    @ViewById
    protected Spinner orderBySpinner;

    @ViewById
    protected Spinner orderSpinner;

    @ViewById
    protected SwipeRefreshLayout swipeContainer;

    @Bean
    protected ProviderFacade provider;

    @Bean
    protected EntityFacadeLocator entityFacadeLocator;

    @ViewById
    public ListView list;

    @ViewById
    public FloatingActionButton fab;

    @ViewById
    public LinearLayout searchbox;

    @InstanceState
    public boolean searchtoggle;

    private EntityFacade<E> entityFacade;

    private final Class<E> entityClass;

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

    protected final EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadMoreData(page);
        }
    };

    protected int page = 1;
    protected CursorAdapterLoader cursorAdapterLoader;

    protected BaseEntityListFragment(Class<E> clazz) {
        this.entityClass = clazz;
    }

    public void loadMoreData(int page) {
        this.page = page;
        restartLoader();
    }

    protected void resetListViewPosition() {
        page = 1;
        listView.smoothScrollToPosition(0);
        endlessScrollListener.resetListener();
    }

    public void setFilterHostId(String filterHostId) {
        this.filterHostId = filterHostId;
    }

    @Override
    public void updateSelectedClusterId(String selectedClusterId) {
        resetListViewPosition();
        this.selectedClusterId = selectedClusterId;
        restartLoader();
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
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeContainer;
    }

    @AfterViews
    protected void initAdapters() {
        CursorAdapter cursorAdapter = createCursorAdapter();

        entityFacade = entityFacadeLocator.getFacade(entityClass);

        listView.setAdapter(cursorAdapter);
        listView.setEmptyView(getActivity().findViewById(android.R.id.empty));
        listView.setTextFilterEnabled(true);

        cursorAdapterLoader = new CursorAdapterLoader(cursorAdapter) {
            @Override
            public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
                ProviderFacade.QueryBuilder<E> query = provider.query(entityClass);

                if (selectedClusterId != null) {
                    query.where(CLUSTER_ID, selectedClusterId);
                }

                if (filterHostId != null){
                    query.where(HOST_ID, filterHostId);
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
                return query.orderBy(orderBy, order).limit(page * ITEMS_PER_PAGE).asLoader();
            }
        };

        getLoaderManager().initLoader(0, null, cursorAdapterLoader);

        listView.setOnScrollListener(endlessScrollListener);

        searchText.removeTextChangedListener(textWatcher);
        searchText.addTextChangedListener(textWatcher);

        RestartOrderItemSelectedListener orderItemSelectedListener = new RestartOrderItemSelectedListener();

        orderBySpinner.setOnItemSelectedListener(orderItemSelectedListener);
        orderSpinner.setOnItemSelectedListener(orderItemSelectedListener);

        fab.setColorPressed(Color.parseColor("#80cbc4"));
        fab.setColorRipple(getResources().getColor(R.color.abc_search_url_text_selected));
        fab.attachToListView(list);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSearchBoxVisibility();
            }
        });
    }
    @UiThread
    public void setSearchBoxVisibility(boolean visible) {
        if(visible) {
            searchbox.setVisibility(View.VISIBLE);
        } else {
            searchbox.setVisibility(View.GONE);
        }
    }

    public void toggleSearchBoxVisibility() {
        searchtoggle = !searchtoggle;
        setSearchBoxVisibility(searchtoggle);
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, cursorAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onResume() {
        super.onResume();

        setRefreshing(SyncAdapter.inSync);

        restartLoader();
        setSearchBoxVisibility(searchtoggle);
    }

    @Override
    public void onPause() {
        super.onPause();

        setRefreshing(false);

        destroyLoader();
    }

    @ItemClick(android.R.id.list)
    protected void itemClicked(Cursor cursor) {
        E entity = entityFacade.mapFromCursor(cursor);
        startActivity(entityFacade.getDetailIntent(entity, getActivity()));
    }

    @Override
    @Background
    public void onRefresh() {
        syncUtils.triggerCoreRefresh();
    }

    @UiThread
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        setRefreshing(syncing);
    }

    protected abstract CursorAdapter createCursorAdapter();
}

