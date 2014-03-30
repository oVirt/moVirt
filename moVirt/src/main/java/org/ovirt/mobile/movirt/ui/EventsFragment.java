package org.ovirt.mobile.movirt.ui;

import android.app.Fragment;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.BaseEntity.ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.CLUSTER_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.VM_ID;

@EFragment(R.layout.fragment_event_list)
public class EventsFragment extends Fragment {

    @ViewById
    ListView list;

    @Bean
    ProviderFacade provider;

    private SimpleCursorAdapter eventListAdapter;

    private CursorAdapterLoader eventsLoader;

    private String filterClusterId;
    private String filterVmId;

    @AfterViews
    void init() {

        eventListAdapter = new SimpleCursorAdapter(getActivity(),
                                                   R.layout.event_list_item,
                                                   null,
                                                   new String[] {OVirtContract.Event.TIME, OVirtContract.Event.DESCRIPTION},
                                                   new int[] {R.id.event_timestamp, R.id.event_description});

        eventsLoader = new CursorAdapterLoader(eventListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                final ProviderFacade.QueryBuilder<Event> query = provider.query(Event.class);
                if (filterClusterId != null) query.where(CLUSTER_ID, filterClusterId);
                if (filterVmId != null) query.where(VM_ID, filterVmId);
                return query.orderByDescending(ID).asLoader();
            }
        };

//        eventListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//            @Override
//            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//                return false;
//            }
//        });
        list.setAdapter(eventListAdapter);

        getLoaderManager().initLoader(0, null, eventsLoader);
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(0, null, eventsLoader);
    }

    public String getFilterClusterId() {
        return filterClusterId;
    }

    public void setFilterClusterId(String filterClusterId) {
        this.filterClusterId = filterClusterId;
    }

    public String getFilterVmId() {
        return filterVmId;
    }

    public void setFilterVmId(String filterVmId) {
        this.filterVmId = filterVmId;
    }
}
