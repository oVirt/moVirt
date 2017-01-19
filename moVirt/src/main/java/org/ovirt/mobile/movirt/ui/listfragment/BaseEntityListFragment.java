package org.ovirt.mobile.movirt.ui.listfragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.EntityFacade;
import org.ovirt.mobile.movirt.facade.EntityFacadeLocator;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.EndlessScrollListener;
import org.ovirt.mobile.movirt.ui.HasLoader;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.CustomSort;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.NamedEntity.NAME;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class BaseEntityListFragment<E extends OVirtEntity> extends RefreshableLoaderFragment
        implements HasLoader {

    private static final int ITEMS_PER_PAGE = 20;
    private static final int ASCENDING_INDEX = 0, DESCENDING_INDEX = 1; // order spinner indexes

    @Bean
    protected SyncUtils syncUtils;

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

    @ViewById
    public LinearLayout orderingLayout;

    @InstanceState
    public boolean searchtoggle;

    @InstanceState
    public int orderBySpinnerPosition;

    @InstanceState
    public int orderSpinnerPosition;

    protected EntityFacade<E> entityFacade;

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

    public void setOrderingSpinners(String orderBy, SortOrder order) {
        if (order == null || orderBy == null) {
            return;
        }

        for (int i = 0; i < orderBySpinner.getCount(); i++) {
            SortEntry sortEntry = (SortEntry) orderBySpinner.getItemAtPosition(i);
            if (sortEntry.getItemName().getColumnName().equals(orderBy)) {
                orderSpinnerPosition = (order == SortOrder.ASCENDING) ? ASCENDING_INDEX : DESCENDING_INDEX;
                orderSpinner.setSelection(AdapterView.INVALID_POSITION); // gets reset by orderBySpinner

                orderBySpinner.setSelection(i);
                break;
            }
        }
    }

    class OrderItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            orderSpinnerPosition = i;
            resetListViewPosition();
            restartLoader();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class OrderByItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            orderBySpinnerPosition = i;

            int orderPosition = orderSpinner.getSelectedItemPosition();
            if (orderPosition == AdapterView.INVALID_POSITION) {
                orderPosition = orderSpinnerPosition;
            }

            final SortEntry orderBy = (SortEntry) adapterView.getSelectedItem();
            final SortOrderType sortOrderType = orderBy.getSortOrder();

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, new String[]{
                    sortOrderType.getAscDisplayName(), // ASCENDING_INDEX
                    sortOrderType.getDescDisplayName() // DESCENDING_INDEX
            });
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            orderSpinner.setAdapter(spinnerArrayAdapter);
            orderSpinner.setSelection(orderPosition); // refreshes loader
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
    protected void init() {
        entityFacade = entityFacadeLocator.getFacade(entityClass);

        initSpinners();
        initAdapters();
        initListeners();
    }

    private void initSpinners() {
        if (getCustomSort() == null) {
            ArrayAdapter<SortEntry> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, getSortEntries());
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            orderBySpinner.setAdapter(spinnerArrayAdapter);
            orderBySpinner.setSelection(orderBySpinnerPosition); // initializes orderSpinner
        } else {
            orderingLayout.setVisibility(View.GONE);
        }
    }

    protected void initAdapters() {
        final CursorAdapter cursorAdapter = createCursorAdapter();

        listView.setAdapter(cursorAdapter);
        listView.setEmptyView(getActivity().findViewById(android.R.id.empty));
        listView.setTextFilterEnabled(true);

        cursorAdapterLoader = new CursorAdapterLoader(cursorAdapter) {
            @Override
            public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
                ProviderFacade.QueryBuilder<E> query = provider.query(entityClass);

                appendQuery(query);

                String searchNameString = searchText.getText().toString();
                if (!StringUtils.isEmpty(searchNameString)) {
                    query.whereLike(NAME, "%" + searchNameString + "%");
                }

                final CustomSort customSort = getCustomSort();

                if (customSort == null) {
                    final SortEntry orderBy = (SortEntry) orderBySpinner.getSelectedItem();
                    final SortOrder order = SortOrderType.getSortOrder((String) orderSpinner.getSelectedItem());
                    query.orderBy(orderBy.getItemName().getColumnName(), order);
                } else {
                    for (CustomSort.CustomSortEntry entry : customSort.getSortEntries()) {
                        query.orderBy(entry.getColumnName(), entry.getSortOrder());
                    }
                }

                return query.limit(page * ITEMS_PER_PAGE).asLoader();
            }
        };

        getLoaderManager().initLoader(0, null, cursorAdapterLoader);
    }

    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        // left intentionally empty
    }

    protected void initListeners() {
        listView.setOnScrollListener(endlessScrollListener);

        searchText.removeTextChangedListener(textWatcher);
        searchText.addTextChangedListener(textWatcher);

        orderBySpinner.setOnItemSelectedListener(new OrderByItemSelectedListener());
        orderSpinner.setOnItemSelectedListener(new OrderItemSelectedListener());

        fab.setColorPressed(Color.parseColor("#80cbc4"));
        fab.setColorRipple(getResources().getColor(R.color.abc_search_url_text_selected));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSearchBoxVisibility();
            }
        });
    }

    public CustomSort getCustomSort() {
        return null;
    }

    public SortEntry[] getSortEntries() {
        return new SortEntry[]{};
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setSearchBoxVisibility(boolean visible) {
        if (visible) {
            searchbox.setVisibility(View.VISIBLE);
        } else {
            searchbox.setVisibility(View.GONE);
        }
    }

    public void toggleSearchBoxVisibility() {
        searchtoggle = !searchtoggle;
        setSearchBoxVisibility(searchtoggle);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
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

        restartLoader();
        setSearchBoxVisibility(searchtoggle);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @ItemClick(android.R.id.list)
    protected void itemClicked(Cursor cursor) {
        if (entityFacade != null) {
            E entity = entityFacade.mapFromCursor(cursor);
            Intent intent = entityFacade.getDetailIntent(entity, getActivity());
            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    @Override
    @Background
    public void onRefresh() {
        if (entityFacade != null) {
            entityFacade.syncAll(new ProgressBarResponse<List<E>>(this));
        }
    }

    protected abstract CursorAdapter createCursorAdapter();
}

