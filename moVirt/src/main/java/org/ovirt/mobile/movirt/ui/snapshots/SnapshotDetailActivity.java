package org.ovirt.mobile.movirt.ui.snapshots;

import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;

@EActivity(R.layout.activity_snapshot_detail)
public class SnapshotDetailActivity extends MovirtActivity implements HasProgressBar {

    private static final String TAG = SnapshotDetailActivity.class.getSimpleName();
    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringArrayRes(R.array.snapshot_detail_pager_titles)
    String[] PAGER_TITLES;
    @ViewById
    ProgressBar progress;

    private String snapshotId = null;
    private Snapshot currentSnapshot = null;

    @AfterViews
    void init() {
        Uri snapshotUri = getIntent().getData();
        snapshotId = snapshotUri.getLastPathSegment();

        initPagers();
        setProgressBar(progress);
    }

    private void initPagers() {

//        VmsFragment vmsFragment = new VmsFragment_();
//        EventsFragment eventsFragment = new EventsFragment_();

//        vmsFragment.setFilterHostId(snapshotId);
//        eventsFragment.setFilterHostId(snapshotId);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES
        //       new HostDetailGeneralFragment_()
        );

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }


    /** Refreshes Snapshot upon success */
   /* private class SyncHostResponse extends OVirtClient.SimpleResponse<Void> {
        @Override
        public void onResponse(Void aVoid) throws RemoteException {
            syncHost();
        }
    }*/
}
