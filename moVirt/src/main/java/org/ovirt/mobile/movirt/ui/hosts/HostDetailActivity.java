package org.ovirt.mobile.movirt.ui.hosts;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.enums.HostCommand;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.events.EventsFragment;
import org.ovirt.mobile.movirt.ui.events.EventsFragment_;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment_;

@EActivity(R.layout.activity_host_detail)
@OptionsMenu(R.menu.host)
public class HostDetailActivity extends MovirtActivity
        implements HasProgressBar, LoaderManager.LoaderCallbacks<Cursor>,
        ConfirmDialogFragment.ConfirmDialogListener {

    private static final String TAG = HostDetailActivity.class.getSimpleName();

    private static final int HOSTS_LOADER = 1; // 0 in MovirtActivity
    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringRes(R.string.details_for_host)
    String HOST_DETAILS;
    @StringArrayRes(R.array.host_detail_pager_titles)
    String[] PAGER_TITLES;
    @ViewById
    ProgressBar progress;
    @Bean
    OVirtClient client;
    @Bean
    HostFacade hostFacade;
    @Bean
    ProviderFacade provider;
    @App
    MoVirtApp app;
    @OptionsMenuItem(R.id.action_activate)
    MenuItem menuActivate;
    @OptionsMenuItem(R.id.action_deactivate)
    MenuItem menuDeactivate;
    private String hostId = null;
    private Host currentHost = null;

    @AfterViews
    void init() {
        Uri hostUri = getIntent().getData();
        hostId = hostUri.getLastPathSegment();

        initLoaders();
        initPagers();
        setProgressBar(progress);
    }

    private void initPagers() {
        VmsFragment vmsFragment = new VmsFragment_();
        EventsFragment eventsFragment = new EventsFragment_();

        vmsFragment.setHostId(hostId);
        eventsFragment.setFilterHostId(hostId);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new HostDetailGeneralFragment_(),
                vmsFragment,
                eventsFragment);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    void initLoaders() {
        getSupportLoaderManager().initLoader(HOSTS_LOADER, null, this);
    }

    @Override
    public void restartLoader() {
        super.restartLoader();
        getSupportLoaderManager().restartLoader(HOSTS_LOADER, null, this);
    }

    @Override
    public void destroyLoader() {
        super.destroyLoader();
        getSupportLoaderManager().destroyLoader(HOSTS_LOADER);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;

        switch (id) {
            case HOSTS_LOADER:
                loader = provider.query(Host.class).id(hostId).asLoader();
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Host");
            return;
        }

        switch (loader.getId()) {
            case HOSTS_LOADER:
                currentHost = hostFacade.mapFromCursor(data);
                invalidateOptionsMenu();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentHost != null) {
            setTitle(String.format(HOST_DETAILS, currentHost.getName()));
            menuActivate.setVisible(HostCommand.ACTIVATE.canExecute(currentHost.getStatus()));
            menuDeactivate.setVisible(HostCommand.DEACTIVATE.canExecute(currentHost.getStatus()));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_activate)
    @Background
    void activate() {
        client.activateHost(hostId, new SyncHostResponse());
    }

    @OptionsItem(R.id.action_deactivate)
    void deactivate() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(0, getString(R.string.dialog_action_deactivate_host));
        confirmDialog.show(getFragmentManager(), "confirmDeactivateHost");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (dialogButton == DialogInterface.BUTTON_POSITIVE) {
            doDeactivate();
        }
    }

    @Background
    void doDeactivate() {
        client.dectivateHost(hostId, new SyncHostResponse());
    }

    private void syncHost() {
        hostFacade.syncOne(new ProgressBarResponse<Host>(this), hostId);
    }

    /**
     * Refreshes Host upon success
     */
    private class SyncHostResponse extends SimpleResponse<Void> {
        @Override
        public void onResponse(Void aVoid) throws RemoteException {
            syncHost();
        }
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }
}
