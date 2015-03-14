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
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableFragment;

@EFragment(R.layout.fragment_host_detail_general)
public class HostDetailGeneralFragment extends RefreshableFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = HostDetailGeneralFragment.class.getSimpleName();

    private static final String HOST_URI = "host_uri";

    private String hostId = null;

    @ViewById
    TextView statusView;

    @ViewById
    SwipeRefreshLayout swipeGeneralContainer;

    @StringRes(R.string.details_for_host)
    String HOST_DETAILS;

    @Bean
    ProviderFacade provider;

    @Bean
    HostFacade hostFacade;

    private Bundle args;

    @AfterViews
    void initLoader() {
        Uri hostUri = getActivity().getIntent().getData();
        hostId = hostUri.getLastPathSegment();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeGeneralContainer;
    }

    @Override
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
