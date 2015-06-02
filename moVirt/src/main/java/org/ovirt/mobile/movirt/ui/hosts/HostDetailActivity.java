package org.ovirt.mobile.movirt.ui.hosts;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.EventsFragment;
import org.ovirt.mobile.movirt.ui.EventsFragment_;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment_;

@EActivity(R.layout.activity_host_detail)
public class HostDetailActivity extends ActionBarActivity {
    private static final String TAG = HostDetailActivity.class.getSimpleName();

    private String hostId = null;

    @ViewById
    ViewPager viewPager;

    @ViewById
    PagerTabStrip pagerTabStrip;

    @StringArrayRes(R.array.host_detail_pager_titles)
    String[] PAGER_TITLES;

    @AfterViews
    void init() {

        initPagers();
    }

    private void initPagers(){
        Intent intent = getIntent();
        String message = intent.getStringExtra("EXTRA_HOST_ID");
        if (message != null) {
            hostId = message;
        } else {
            Uri hostUri = intent.getData();
            hostId = hostUri.getLastPathSegment();
        }

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
}
