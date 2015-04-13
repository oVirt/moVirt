package org.ovirt.mobile.movirt.ui.hosts;

import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;

@EActivity(R.layout.activity_host_detail)
public class HostDetailActivity extends ActionBarActivity {
    private static final String TAG = HostDetailActivity.class.getSimpleName();

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

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new HostDetailGeneralFragment_());

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }
}
