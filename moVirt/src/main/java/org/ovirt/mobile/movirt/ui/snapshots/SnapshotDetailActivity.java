package org.ovirt.mobile.movirt.ui.snapshots;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;
import org.ovirt.mobile.movirt.ui.vms.VmDetailGeneralFragment;
import org.ovirt.mobile.movirt.ui.vms.VmDetailGeneralFragment_;

import java.util.Collection;
import java.util.Iterator;

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

    @StringRes(R.string.details_for_snapshot)
    String SNAPSHOT_DETAILS;

    @Bean
    ProviderFacade provider;

    private String snapshotId = null;
    private Snapshot currentSnapshot = null;

    @AfterViews
    void init() {
        Intent intent = getIntent();
        Uri snapshotUri = intent.getData();
        snapshotId = snapshotUri.getLastPathSegment();

        Collection<Snapshot> snapshots = provider.query(Snapshot.class).id(snapshotId).all();
        Iterator<Snapshot> it = snapshots.iterator();

        if (it.hasNext()) {
            currentSnapshot = it.next();
            setTitle(String.format(SNAPSHOT_DETAILS, currentSnapshot.getName()));
            String vmId = currentSnapshot.getVmId();
            Uri vmUri = OVirtContract.Vm.CONTENT_URI.buildUpon().appendPath(vmId + snapshotId).build();
            intent.setData(vmUri);
        }

        initPagers();
        setProgressBar(progress);
    }

    private void initPagers() {

        VmDetailGeneralFragment vmDetailFragment = new VmDetailGeneralFragment_();

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                vmDetailFragment
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
