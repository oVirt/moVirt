package org.ovirt.mobile.movirt.ui.hosts;

import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
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
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.EventsFragment;
import org.ovirt.mobile.movirt.ui.EventsFragment_;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.UpdateMenuItemAware;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment_;

@EActivity(R.layout.activity_host_detail)
@OptionsMenu(R.menu.host)
public class HostDetailActivity extends ActionBarActivity implements HasProgressBar, UpdateMenuItemAware {
    private static final String TAG = HostDetailActivity.class.getSimpleName();
    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringArrayRes(R.array.host_detail_pager_titles)
    String[] PAGER_TITLES;
    private String hostId = null;

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

    @AfterViews
    void init() {

        initPagers();
        hideProgressBar();
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

    @OptionsItem(R.id.action_activate)
    @Background
    void activate() {
        client.activateHost(hostId);
        syncHost();
    }

    @OptionsItem(R.id.action_deactivate)
    @Background
    void deactivate() {
        client.dectivateHost(hostId);
        syncHost();
    }

    private void syncHost() {
        hostFacade.sync(hostId, new ProgressBarResponse<Host>(this));
    }

    @UiThread
    @Override
    public void showProgressBar() {
        progress.setVisibility(View.VISIBLE);
    }

    @UiThread
    @Override
    public void hideProgressBar() {
        progress.setVisibility(View.GONE);
    }

    @Override
    public void UpdateMenuItem(OVirtEntity entity) {
        Host host = (Host) entity;

        if (host.getStatus() == Host.Status.UP){
            menuActivate.setVisible(false);
            menuDeactivate.setVisible(true);
        } else if (host.getStatus() == Host.Status.MAINTENANCE){
            menuActivate.setVisible(true);
            menuDeactivate.setVisible(false);
        } else {
            menuActivate.setVisible(false);
            menuDeactivate.setVisible(false);
        }
    }
}
