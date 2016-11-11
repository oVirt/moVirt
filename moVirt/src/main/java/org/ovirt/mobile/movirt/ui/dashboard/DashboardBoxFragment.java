package org.ovirt.mobile.movirt.ui.dashboard;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.GridView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.LoaderFragment;

import java.util.List;

@EFragment(R.layout.fragment_dashboard_box)
public class DashboardBoxFragment extends LoaderFragment implements LoaderManager.LoaderCallbacks<List<DashboardBoxData>> {

    @ViewById
    GridView boxGridView;

    @Bean
    ProviderFacade provider;

    DashboardBoxListAdapter dashboardBoxListAdapter;

    @AfterViews
    void init() {
        dashboardBoxListAdapter = new DashboardBoxListAdapter(getActivity());
        boxGridView.setAdapter(dashboardBoxListAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public Loader<List<DashboardBoxData>> onCreateLoader(int id, Bundle args) {
        return new DashboardBoxDataLoader(getActivity(), provider);
    }

    @Override
    public void onLoadFinished(Loader<List<DashboardBoxData>> loader, List<DashboardBoxData> data) {
        dashboardBoxListAdapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<DashboardBoxData>> loader) {
        dashboardBoxListAdapter.setData(null);
    }
}
