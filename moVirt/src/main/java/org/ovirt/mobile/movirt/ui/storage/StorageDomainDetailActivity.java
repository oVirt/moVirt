package org.ovirt.mobile.movirt.ui.storage;

import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.widget.ProgressBar;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.EventsFragment;
import org.ovirt.mobile.movirt.ui.EventsFragment_;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;

@EActivity(R.layout.activity_storage_domain_detail)
public class StorageDomainDetailActivity extends MovirtActivity implements HasProgressBar {

    private static final String TAG = StorageDomainDetailActivity.class.getSimpleName();

    @ViewById
    ViewPager viewPager;

    @ViewById
    PagerTabStrip pagerTabStrip;

    @StringArrayRes(R.array.storage_domain_detail_pager_titles)
    String[] PAGER_TITLES;

    @ViewById
    ProgressBar progress;

    @AfterViews
    void init() {

        initPagers();
        setProgressBar(progress);
    }

    private void initPagers() {
        Uri storageDomainUri = getIntent().getData();
        String storageDomainId = storageDomainUri.getLastPathSegment();

        EventsFragment eventsFragment = new EventsFragment_();
        eventsFragment.setFilterStorageDomainId(storageDomainId);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new StorageDomainDetailGeneralFragment_(),
                eventsFragment);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }
}
