package org.ovirt.mobile.movirt.ui.dashboard;

import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.BooleanRes;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.PresenterStatusSyncableActivity;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;

@EActivity(R.layout.activity_dashboard)
public class DashboardActivity extends PresenterStatusSyncableActivity implements DashboardContract.View {

    @ViewById
    ViewPager viewPager;

    @ViewById
    PagerTabStrip pagerTabStrip;

    @StringArrayRes(R.array.phone_dashboard_pager_titles)
    String[] PHONE_PAGER_TITLES;

    @StringArrayRes(R.array.tablet_dashboard_pager_titles)
    String[] TABLET_PAGER_TITLES;

    @ViewById
    ProgressBar progress;

    @App
    MoVirtApp app;

    @BooleanRes
    boolean isTablet;

    private DashboardContract.Presenter dashboardPresenter;

    @AfterViews
    void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initPagers();
        setProgressBar(progress);

        dashboardPresenter = DashboardPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .initialize();
    }

    @Override
    public BasePresenter getPresenter() {
        return dashboardPresenter;
    }

    private void initPagers() {

        DashboardContainer physicalDashboard = new DashboardContainer_();
        DashboardContainer virtualDashboard = new DashboardContainer_();
        virtualDashboard.setDashboardType(DashboardType.VIRTUAL);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), isTablet ? TABLET_PAGER_TITLES : PHONE_PAGER_TITLES,
                physicalDashboard,
                virtualDashboard
        );

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }
}
