package org.ovirt.mobile.movirt.ui;

import android.app.Fragment;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.BaseEntity.ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.CLUSTER_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.VM_ID;

@EFragment(R.layout.fragment_event_list)
public class EventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @ViewById
    ListView list;

    @Bean
    ProviderFacade provider;

    @Bean
    EventsHandler eventsHandler;

    @InstanceState
    String filterClusterId;

    @InstanceState
    String filterVmId;

    @Bean
    SyncUtils syncUtils;

    @ViewById
    SwipeRefreshLayout swipeEventsContainer;

    private int page = 1;
    private static final int EVENTS_PER_PAGE = 20;
    private static final String TAG = EventsFragment.class.getSimpleName();

    private EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadMoreData(page);
        }
    };

    private CursorAdapterLoader cursorAdapterLoader;

    @AfterViews
    void init() {
        swipeEventsContainer.setOnRefreshListener(this);

        SimpleCursorAdapter eventListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.event_list_item,
                null,
                new String[]{OVirtContract.Event.TIME, OVirtContract.Event.DESCRIPTION},
                new int[]{R.id.event_timestamp, R.id.event_description}, 0);

        list.setAdapter(eventListAdapter);

        cursorAdapterLoader = new CursorAdapterLoader(eventListAdapter) {
            @Override
            public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
                final ProviderFacade.QueryBuilder<Event> query = provider.query(Event.class);
                if (filterClusterId != null) query.where(CLUSTER_ID, filterClusterId);
                if (filterVmId != null) query.where(VM_ID, filterVmId);
                return query.orderByDescending(ID).limit(page * EVENTS_PER_PAGE).asLoader();
            }
        };

        getLoaderManager().initLoader(0, null, cursorAdapterLoader);

        list.setOnScrollListener(endlessScrollListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (EventsHandler.inSync) {
            showProgress();
        }
        restartLoader();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgress();
    }

    public void updateFilterClusterIdTo(String filterClusterId) {
        this.filterClusterId = filterClusterId;
        page = 1;
        list.setSelectionAfterHeaderView();
        endlessScrollListener.resetListener();
        restartLoader();
    }

    public void setFilterVmId(String filterVmId) {
        this.filterVmId = filterVmId;
    }

    public void loadMoreData(int page) {
        this.page = page;
        restartLoader();
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, cursorAdapterLoader);
    }

    @UiThread
    void showProgress() {
        swipeEventsContainer.setRefreshing(true);
    }

    @UiThread
    void hideProgress() {
        swipeEventsContainer.setRefreshing(false);
    }

    @Receiver(actions = Broadcasts.EVENTS_IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void eventsSyncing(@Receiver.Extra((Broadcasts.Extras.SYNCING)) boolean syncing) {
        swipeEventsContainer.setRefreshing(syncing);
    }

    @Background
    @Override
    public void onRefresh() {
//        eventsHandler.deleteEvents();
        eventsHandler.updateEvents(true);
    }
}
