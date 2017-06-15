package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Context;
import android.content.Intent;
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
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.EndlessScrollListener;
import org.ovirt.mobile.movirt.ui.LoaderFragment;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;
import org.ovirt.mobile.movirt.util.Disposables;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.ovirt.mobile.movirt.provider.OVirtContract.AccountNamedEntity.NAME;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.CPU_USAGE;

@EFragment(R.layout.fragment_dashboard_most_utilized)
public class DashboardMostUtilizedFragment extends LoaderFragment {
    private final static String TAG = DashboardMostUtilizedFragment.class.getSimpleName();

    private static final int ACTIVE_LOADER = 1;
    private static final int ITEMS_PER_PAGE = 20;

    @Bean
    VmFacade vmFacade;

    @Bean
    HostFacade hostFacade;

    @Bean
    ProviderFacade provider;

    @Bean
    AccountRxStore rxStore;

    @ViewById
    ListView listView;

    @ViewById
    TextView mostUtilizedText;

    private CursorAdapterLoader cursorAdapterLoader;

    @InstanceState
    DashboardType dashboardType = DashboardType.PHYSICAL;

    private int page = 1;

    private Disposables disposables = new Disposables();

    private ActiveSelection activeSelection = ActiveSelection.ALL_ACTIVE;

    @AfterViews
    void init() {
        render();
        listView.setOnScrollListener(endlessScrollListener);
        disposables.add(rxStore.ACTIVE_SELECTION
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(selection -> {
                    activeSelection = selection;
                    restartLoader();
                }));
    }

    @Override
    public void onDestroyView() {
        disposables.destroy();
        super.onDestroyView();
    }

    public void setDashboardType(DashboardType dashboardType) {
        this.dashboardType = dashboardType;
    }

    private void setLoader() {
        CursorAdapter listAdapter = new MostUtilizedListAdapter(getActivity(), null, ACTIVE_LOADER);

        if (dashboardType == DashboardType.PHYSICAL) {
            cursorAdapterLoader = new CursorAdapterLoader(listAdapter) {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                    return DashboardHelper.querySelection(provider, Host.class, activeSelection)
                            .orderByDescending(CPU_USAGE)
                            .limit(page * ITEMS_PER_PAGE)
                            .asLoader();
                }
            };
        } else {
            cursorAdapterLoader = new CursorAdapterLoader(listAdapter) {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                    return DashboardHelper.querySelection(provider, Vm.class, activeSelection)
                            .orderByDescending(CPU_USAGE)
                            .limit(page * ITEMS_PER_PAGE)
                            .asLoader();
                }
            };
        }

        listView.setAdapter(listAdapter);
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(ACTIVE_LOADER, null, cursorAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(ACTIVE_LOADER);
    }

    @ItemClick(R.id.listView)
    protected void itemClicked(Cursor cursor) {
        Intent intent;

        if (dashboardType == DashboardType.PHYSICAL) {
            Host host = hostFacade.mapFromCursor(cursor);
            intent = hostFacade.getIntentResolver().getDetailIntent(host, getActivity());
        } else {
            Vm vm = vmFacade.mapFromCursor(cursor);
            intent = vmFacade.getIntentResolver().getDetailIntent(vm, getActivity());
        }

        startActivity(intent);
    }

    protected final EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int pageNum, int totalItemsCount) {
            page = pageNum;
            getLoaderManager().restartLoader(ACTIVE_LOADER, null, cursorAdapterLoader);
        }
    };

    public void render() {
        String utilizedText = getString(dashboardType == DashboardType.PHYSICAL ? R.string.most_utilized_hosts : R.string.most_utilized_vms);
        mostUtilizedText.setText(utilizedText);
        setLoader();
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
                    usageView.setTextColor(Color.parseColor("#3f9c35"));
                } else if (Double.compare(Double.parseDouble(usage), 75) < 0) {
                    usageView.setTextColor(Color.parseColor("#ec7a08"));
                } else {
                    usageView.setTextColor(Color.parseColor("#cc0000"));
                }
            } catch (Exception e) {
            }
        }
    }
}
