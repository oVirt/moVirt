package org.ovirt.mobile.movirt.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import static org.ovirt.mobile.movirt.provider.OVirtContract.BaseEntity.ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.CLUSTER_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.VM_ID;

@EFragment(R.layout.fragment_event_list)
public class EventsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    @ViewById
    ListView list;

    @ViewById
    ProgressBar eventsProgress;

    @Bean
    ProviderFacade provider;

    @Bean
    EventsHandler eventsHandler;

    private SimpleCursorAdapter eventListAdapter;

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

    @AfterViews
    void init() {
        swipeEventsContainer.setOnRefreshListener(this);

        eventListAdapter = new SimpleCursorAdapter(getActivity(),
                                                   R.layout.event_list_item,
                                                   null,
                                                   new String[] {OVirtContract.Event.TIME, OVirtContract.Event.DESCRIPTION},
                                                   new int[] {R.id.event_timestamp, R.id.event_description});

        list.setAdapter(eventListAdapter);

        getLoaderManager().initLoader(0, null, this);

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

    @Override
    public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final ProviderFacade.QueryBuilder<Event> query = provider.query(Event.class);
        if (filterClusterId != null) query.where(CLUSTER_ID, filterClusterId);
        if (filterVmId != null) query.where(VM_ID, filterVmId);
        return query.orderByDescending(ID).limit(page * EVENTS_PER_PAGE).asLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor cursor) {
        if(eventListAdapter != null && cursor != null) {
            eventListAdapter.swapCursor(cursor); //swap the new cursor in.
        }
        else {
            Log.v(TAG, "OnLoadFinished: eventListAdapter is null");
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(eventListAdapter!=null) {
            eventListAdapter.swapCursor(null);
        }
        else {
            Log.v(TAG, "OnLoadFinished: eventListAdapter is null");
        }
    }

    public void loadMoreData(int page) {
        this.page = page;
        restartLoader();
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @UiThread
    void showProgress() {
        eventsProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    void hideProgress() {
        eventsProgress.setVisibility(View.GONE);
    }

    @Receiver(actions = Broadcasts.EVENTS_IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void eventsSyncing(@Receiver.Extra((Broadcasts.Extras.SYNCING)) boolean syncing) {
        if (syncing) {
            showProgress();
        } else {
            hideProgress();
        }
    }

    @Background
    @Override
    public void onRefresh() {
//        eventsHandler.deleteEvents();
        eventsHandler.updateEvents(true);
    }
}
