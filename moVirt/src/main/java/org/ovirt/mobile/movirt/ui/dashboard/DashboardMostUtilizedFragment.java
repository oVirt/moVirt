package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.EndlessScrollListener;
import org.ovirt.mobile.movirt.ui.LoaderFragment;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotEmbeddableEntity.SNAPSHOT_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.CPU_USAGE;

@EFragment(R.layout.fragment_dashboard_most_utilized)
public class DashboardMostUtilizedFragment extends LoaderFragment implements OVirtContract.NamedEntity {
    private final static String TAG = DashboardMostUtilizedFragment.class.getSimpleName();

    private static final int VM_LOADER = 1;
    private static final int HOST_LOADER = 2;

    private static final int ITEMS_PER_PAGE = 20;

    @Bean
    VmFacade vmFacade;

    @Bean
    HostFacade hostFacade;

    @Bean
    ProviderFacade provider;

    @ViewById
    ListView vmListView;

    @ViewById
    ListView hostListView;

    private CursorAdapterLoader vmCursorAdapterLoader;
    private CursorAdapterLoader hostCursorAdapterLoader;
    private int vmPage = 1;
    private int hostPage = 1;

    @AfterViews
    void init() {
        CursorAdapter vmListAdapter = new MostUtilizedListAdapter(getActivity(), null, VM_LOADER);
        vmListView.setAdapter(vmListAdapter);
        vmCursorAdapterLoader = new CursorAdapterLoader(vmListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                return provider.query(Vm.class).empty(SNAPSHOT_ID).orderByDescending(CPU_USAGE).limit(vmPage * ITEMS_PER_PAGE).asLoader();
            }
        };

        CursorAdapter hostListAdapter = new MostUtilizedListAdapter(getActivity(), null, HOST_LOADER);
        hostListView.setAdapter(hostListAdapter);
        hostCursorAdapterLoader = new CursorAdapterLoader(hostListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                return provider.query(Host.class).orderByDescending(CPU_USAGE).limit(hostPage * ITEMS_PER_PAGE).asLoader();
            }
        };

        getLoaderManager().initLoader(VM_LOADER, null, vmCursorAdapterLoader);
        getLoaderManager().initLoader(HOST_LOADER, null, hostCursorAdapterLoader);

        vmListView.setOnScrollListener(vmEndlessScrollListener);
        hostListView.setOnScrollListener(hostEndlessScrollListener);
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(VM_LOADER, null, vmCursorAdapterLoader);
        getLoaderManager().restartLoader(HOST_LOADER, null, hostCursorAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(VM_LOADER);
        getLoaderManager().destroyLoader(HOST_LOADER);
    }

    @ItemClick(R.id.vmListView)
    protected void vmItemClicked(Cursor cursor) {
        Vm vm = vmFacade.mapFromCursor(cursor);
        startActivity(vmFacade.getDetailIntent(vm, getActivity()));
    }

    @ItemClick(R.id.hostListView)
    protected void hostItemClicked(Cursor cursor) {
        Host host = hostFacade.mapFromCursor(cursor);
        startActivity(hostFacade.getDetailIntent(host, getActivity()));
    }

    protected final EndlessScrollListener vmEndlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadMoreVmData(page);
        }
    };

    public void loadMoreVmData(int page) {
        this.vmPage = page;
        getLoaderManager().restartLoader(VM_LOADER, null, vmCursorAdapterLoader);
    }

    protected final EndlessScrollListener hostEndlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadMoreHostData(page);
        }
    };

    public void loadMoreHostData(int page) {
        this.hostPage = page;
        getLoaderManager().restartLoader(HOST_LOADER, null, hostCursorAdapterLoader);
    }

    private static class MostUtilizedListAdapter extends CursorAdapter {
        private int flagLoader;

        public MostUtilizedListAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            this.flagLoader = flags;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View item = inflater.inflate(R.layout.dashboard_most_utilized_item, null);
            return item;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String name = cursor.getString(cursor.getColumnIndex(NAME));
            String usage = cursor.getString(cursor.getColumnIndex(CPU_USAGE));

            TextView nameView = (TextView) view.findViewById(R.id.name);
            TextView usageView = (TextView) view.findViewById(R.id.usage);

            nameView.setText(name);
            usageView.setText(usage + "%");

            try {
                if (Double.compare(Double.parseDouble(usage), 50) < 0) {
                    usageView.setTextColor(Color.parseColor("#99cc03"));
                } else if (Double.compare(Double.parseDouble(usage), 75) < 0) {
                    usageView.setTextColor(Color.parseColor("#ec7108"));
                } else {
                    usageView.setTextColor(Color.parseColor("#ce0000"));
                }
            } catch (Exception e) {
            }
        }
    }
}
