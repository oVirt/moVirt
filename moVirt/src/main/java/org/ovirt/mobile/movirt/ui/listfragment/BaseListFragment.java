package org.ovirt.mobile.movirt.ui.listfragment;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.model.base.BaseEntity;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.EndlessScrollListener;
import org.ovirt.mobile.movirt.ui.HasLoader;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.CustomSort;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class BaseListFragment<E extends BaseEntity<?>> extends RefreshableLoaderFragment
        implements HasLoader, BaseListFragmentContract.View {

    private static final int BASE_LOADER = 0;

    private static final int ITEMS_PER_PAGE = 20;

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
    protected EnvironmentStore environmentStore;

    @ViewById
    public FloatingActionButton fab;

    @ViewById
    public LinearLayout searchbox;

    @ViewById
    public LinearLayout orderingLayout;

    @Bean
    public AccountRxStore rxStore;

    @InstanceState
    public boolean searchToggle;

    @InstanceState
    public int orderSpinnerPosition = AdapterView.INVALID_POSITION;

    @InstanceState
    public int orderBySpinnerPosition = AdapterView.INVALID_POSITION;

    protected final Class<E> entityClass;

    protected BaseListFragmentContract.Presenter presenter;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            refreshDisplay();
        }
    };

    protected final EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadMoreData(page);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            BaseListFragment.this.onScrollStateChanged(view, scrollState);
        }
    };

    protected int page = 1;
    protected CursorAdapterLoader cursorAdapterLoader;

    protected BaseListFragment(Class<E> clazz) {
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

    class OrderItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            orderSpinnerPosition = i;
            presenter.onOrderSelected(SortOrder.fromIndex(i));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class OrderByItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            orderBySpinnerPosition = i;
            presenter.onOrderBySelected((SortEntry) adapterView.getSelectedItem());
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
        initSpinners();
        initAdapters();
        initListeners();

        presenter = BaseListFragmentPresenter_.getInstance_(getActivity().getApplicationContext())
                .initAllSortEntries(getSortEntries())
                .initSelection(asSortEntry(orderBySpinnerPosition), SortOrder.fromIndex(orderSpinnerPosition))
                .setView(this)
                .initialize();
    }

    private void initSpinners() {
        if (getCustomSort() == null) {
            ArrayAdapter<SortEntry> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, getSortEntries());
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            orderBySpinner.setAdapter(spinnerArrayAdapter);
        } else {
            orderingLayout.setVisibility(View.GONE);
        }
    }

    public void swapOrderSpinner(SortEntry orderBy) {

        final SortOrderType sortOrderType = orderBy.getSortOrder();

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, new String[]{
                sortOrderType.getDisplayNameBySortOrder(SortOrder.fromIndex(0)), // ASCENDING
                sortOrderType.getDisplayNameBySortOrder(SortOrder.fromIndex(1)) // DESCENDING
        });
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(spinnerArrayAdapter);
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

                final CustomSort customSort = getCustomSort();

                if (customSort == null) {
                    final SortEntry orderBy = (SortEntry) orderBySpinner.getSelectedItem();
                    final SortOrder order = SortOrderType.getSortOrder((String) orderSpinner.getSelectedItem());
                    query.orderBy(orderBy.orderBySql(), order);
                } else {
                    for (CustomSort.CustomSortEntry entry : customSort.getSortEntries()) {
                        query.orderBy(entry.getColumnName(), entry.getSortOrder());
                    }
                }

                return query.limit(page * ITEMS_PER_PAGE).asLoader();
            }
        };

        getLoaderManager().initLoader(BASE_LOADER, null, cursorAdapterLoader);
    }

    protected void initListeners() {
        listView.setOnScrollListener(endlessScrollListener);
        listView.setOnItemLongClickListener(getOnItemLongClickListener());

        searchText.removeTextChangedListener(textWatcher);
        searchText.addTextChangedListener(textWatcher);

        if (getCustomSort() == null) {
            orderBySpinner.setOnItemSelectedListener(new OrderByItemSelectedListener());
            orderSpinner.setOnItemSelectedListener(new OrderItemSelectedListener());
        }

        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.material_blue_400)));
        fab.setOnClickListener(view -> toggleSearchBoxVisibility());
    }

    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        // left intentionally empty
    }

    protected void onScrollStateChanged(AbsListView view, int scrollState) {
        // left intentionally empty
    }

    protected AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return null;
    }

    protected CustomSort getCustomSort() {
        return null;
    }

    protected SortEntry[] getSortEntries() {
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
        searchToggle = !searchToggle;
        setSearchBoxVisibility(searchToggle);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(BASE_LOADER, null, cursorAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(BASE_LOADER);
    }

    @Override
    public void refreshDisplay() {
        if (isResumed()) {
            resetListViewPosition();
            restartLoader();
        }
    }

    @Override
    public void displaySelection(SortEntry sortEntry, SortOrder sortOrder) {
        orderBySpinner.setSelection(asSortEntryIndex(sortEntry));
        swapOrderSpinner(sortEntry);
        orderSpinner.setSelection(sortOrder.getIndex());
    }

    @Override
    public BaseListFragmentContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        setSearchBoxVisibility(searchToggle);
        super.onResume();
    }

    protected abstract CursorAdapter createCursorAdapter();

    private SortEntry asSortEntry(int index) {
        final SortEntry[] sortEntries = getSortEntries();
        if (index >= 0 && index < sortEntries.length) {
            return sortEntries[index];
        }
        return null;
    }

    private int asSortEntryIndex(SortEntry sortEntry) {
        final SortEntry[] sortEntries = getSortEntries();
        for (int i = 0; i < sortEntries.length; i++) {
            SortEntry entry = sortEntries[i];
            if (entry.equals(sortEntry)) {
                return i;
            }
        }
        return 0;
    }
}

