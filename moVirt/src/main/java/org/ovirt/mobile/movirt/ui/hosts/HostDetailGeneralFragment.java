package org.ovirt.mobile.movirt.ui.hosts;

import android.content.Intent;
import android.database.Cursor;
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
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

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

    @Bean
    ProviderFacade provider;

    @Bean
    EnvironmentStore environmentStore;

    private HostFacade hostFacade;

    @AfterViews
    void initLoader() {
        Intent intent = getActivity().getIntent();
        hostId = intent.getData().getLastPathSegment();
        MovirtAccount movirtAccount = intent.getParcelableExtra(Constants.ACCOUNT_KEY);
        hostFacade = environmentStore.getEnvironment(movirtAccount).getFacade(Host.class);

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
        hostFacade.syncOne(new ProgressBarResponse<>(this), hostId);
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
        statusView.setText(host.getStatus().toString().toLowerCase());
        cpuView.setText(getString(R.string.percentage, host.getCpuUsage()));
        memView.setText(getString(R.string.percentage, host.getMemoryUsage()));
        long memory = host.getMemorySize();
        memoryView.setText((memory == -1) ? getString(R.string.NA) : new MemorySize(memory).toString());
        summaryView.setText(getString(R.string.three_separated_ints, host.getActive(), host.getMigrating(), host.getTotal()));
        socketView.setText(String.valueOf(host.getSockets()));
        coreView.setText(String.valueOf(host.getCoresPerSocket()));
        threadView.setText(String.valueOf(host.getThreadsPerCore()));
        osVersionView.setText(host.getOsVersion());
        addressView.setText(host.getAddress());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
