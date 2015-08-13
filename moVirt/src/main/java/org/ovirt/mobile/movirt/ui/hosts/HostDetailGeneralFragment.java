package org.ovirt.mobile.movirt.ui.hosts;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.ui.UpdateMenuItemAware;

@EFragment(R.layout.fragment_host_detail_general)
public class HostDetailGeneralFragment extends RefreshableLoaderFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = HostDetailGeneralFragment.class.getSimpleName();

    private static final String HOST_URI = "host_uri";

    private String hostId = null;

    @ViewById
    TextView statusView;

    @ViewById
    TextView cpuView;

    @ViewById
    TextView memView;

    @ViewById
    TextView memoryView;

    @ViewById
    TextView summaryView;

    @ViewById
    TextView socketView;

    @ViewById
    TextView coreView;

    @ViewById
    TextView threadView;

    @ViewById
    TextView osVersionView;

    @ViewById
    TextView addressView;

    @ViewById
    SwipeRefreshLayout swipeGeneralContainer;

    @StringRes(R.string.details_for_host)
    String HOST_DETAILS;

    @Bean
    ProviderFacade provider;

    @Bean
    HostFacade hostFacade;

    @AfterViews
    void initLoader() {
        Uri hostUri = getActivity().getIntent().getData();
        hostId = hostUri.getLastPathSegment();

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
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeGeneralContainer;
    }

    @Override
    @Background
    public void onRefresh() {
        hostFacade.sync(hostId, new ProgressBarResponse<Host>(this));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return provider.query(Host.class).id(hostId).asLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Host");
            return;
        }
        renderHost(hostFacade.mapFromCursor(data));
    }

    private void renderHost(Host host) {
        getActivity().setTitle(String.format(HOST_DETAILS, host.getName()));
        statusView.setText(host.getStatus().toString().toLowerCase());
        cpuView.setText(String.format("%.2f%%", host.getCpuUsage()));
        memView.setText(String.format("%.2f%%", host.getMemoryUsage()));
        if(host.getMemorySizeMb() != -1) {
            memoryView.setText(host.getMemorySizeMb() + " MB");
        }
        else {
            memoryView.setText("N/A");
        }
        summaryView.setText(host.getActive() + " | " + host.getMigrating()
                + " | " + host.getTotal());
        socketView.setText(String.valueOf(host.getSockets()));
        coreView.setText(String.valueOf(host.getCoresPerSocket()));
        threadView.setText(String.valueOf(host.getThreadsPerCore()));
        osVersionView.setText(host.getOsVersion());
        addressView.setText(host.getAddress());

        if (getActivity() instanceof UpdateMenuItemAware) {
            ((UpdateMenuItemAware) getActivity()).updateMenuItem(host);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
