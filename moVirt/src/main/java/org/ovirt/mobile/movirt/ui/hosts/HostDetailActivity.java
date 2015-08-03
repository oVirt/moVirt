package org.ovirt.mobile.movirt.ui.hosts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.AreYouSureDialog;
import org.ovirt.mobile.movirt.ui.EventsFragment;
import org.ovirt.mobile.movirt.ui.EventsFragment_;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.UpdateMenuItemAware;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment_;

@EActivity(R.layout.activity_host_detail)
@OptionsMenu(R.menu.host)
public class HostDetailActivity extends MovirtActivity implements HasProgressBar, UpdateMenuItemAware<Host> {
    private static final String TAG = HostDetailActivity.class.getSimpleName();
    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringArrayRes(R.array.host_detail_pager_titles)
    String[] PAGER_TITLES;
    @ViewById
    ProgressBar progress;
    @Bean
    OVirtClient client;
    @Bean
    HostFacade hostFacade;
    @OptionsMenuItem(R.id.action_activate)
    MenuItem menuActivate;
    @OptionsMenuItem(R.id.action_deactivate)
    MenuItem menuDeactivate;
    private String hostId = null;
    private Host.Status currentStatus;

    @AfterViews
    void init() {

        initPagers();
        setProgressBar(progress);
    }

    private void initPagers() {
        Uri hostUri = getIntent().getData();
        hostId = hostUri.getLastPathSegment();

        VmsFragment vmsFragment = new VmsFragment_();
        EventsFragment eventsFragment = new EventsFragment_();

        vmsFragment.setFilterHostId(hostId);
        eventsFragment.setFilterHostId(hostId);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new HostDetailGeneralFragment_(),
                vmsFragment,
                eventsFragment);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentStatus != null) {
            menuActivate.setVisible(Host.Command.ACTIVATE.canExecute(currentStatus));
            menuDeactivate.setVisible(Host.Command.DEACTIVATE.canExecute(currentStatus));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_activate)
    @Background
    void activate() {
        client.activateHost(hostId);
        syncHost();
    }

    @OptionsItem(R.id.action_deactivate)
    @UiThread
    void deactivate() {
        AreYouSureDialog.show(this, getResources(), "put the host to maintenance", new Runnable() {
            @Override
            public void run() {
                doDeactivate();
            }
        });
    }

    @Background
    void doDeactivate() {
        client.dectivateHost(hostId);
        syncHost();
    }

    private void syncHost() {
        hostFacade.sync(hostId, new ProgressBarResponse<Host>(this));
    }

    @UiThread
    @Override
    public void updateMenuItem(Host host) {
        currentStatus = host.getStatus();
        invalidateOptionsMenu();
    }
}
