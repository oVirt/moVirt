package org.ovirt.mobile.movirt.ui.storage;

import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.PresenterStatusSyncableActivity;
import org.ovirt.mobile.movirt.ui.events.StorageDomainEventsFragment;
import org.ovirt.mobile.movirt.ui.events.StorageDomainEventsFragment_;

@EActivity(R.layout.activity_storage_domain_detail)
public class StorageDomainDetailActivity extends PresenterStatusSyncableActivity implements HasProgressBar, StorageDomainDetailContract.View {

    @ViewById
    ViewPager viewPager;

    @ViewById
    PagerTabStrip pagerTabStrip;

    @StringArrayRes(R.array.storage_domain_detail_pager_titles)
    String[] PAGER_TITLES;

    @ViewById
    ProgressBar progress;

    @App
    MoVirtApp app;

    StorageDomainDetailContract.Presenter presenter;

    @AfterViews
    void init() {
        presenter = StorageDomainDetailPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .setStorageDomainId(getIntent().getData().getLastPathSegment())
                .setAccount(getIntent().getParcelableExtra(Constants.ACCOUNT_KEY))
                .initialize();
        initPagers();
        setProgressBar(progress);
    }

    public StorageDomainDetailContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void displayTitle(String title) {
        setTitle(title);
    }

    private void initPagers() {
        Uri storageDomainUri = getIntent().getData();
        String storageDomainId = storageDomainUri.getLastPathSegment();

        StorageDomainEventsFragment eventsFragment = new StorageDomainEventsFragment_();
        eventsFragment.setStorageDomainId(storageDomainId);
        eventsFragment.setAccount(getIntent().getParcelableExtra(Constants.ACCOUNT_KEY));

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new StorageDomainDetailGeneralFragment_(),
                eventsFragment);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }
}
