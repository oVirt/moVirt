package org.ovirt.mobile.movirt.ui;

import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.ui.dashboard.DashboardActivity_;
import org.ovirt.mobile.movirt.ui.dialogs.AccountDialogFragment;
import org.ovirt.mobile.movirt.ui.events.EventsFragment_;
import org.ovirt.mobile.movirt.ui.hosts.HostsFragment_;
import org.ovirt.mobile.movirt.ui.listfragment.BaseListFragment;
import org.ovirt.mobile.movirt.ui.storage.StorageDomainFragment_;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment_;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.NamedEntity.NAME;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends MovirtActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String[] CLUSTER_PROJECTION = new String[]{OVirtContract.Cluster.NAME,
            OVirtContract.Cluster.ID, OVirtContract.Cluster.VERSION,
            OVirtContract.Cluster.DATA_CENTER_ID};
    private static final int CLUSTER_LOADER = FIRST_CHILD_LOADER;

    @StringRes(R.string.needs_configuration)
    String noAccMsg;

    @StringRes(R.string.connection_not_correct)
    String accIncorrectMsg;

    @StringRes(R.string.all_clusters)
    String allClustersMsg;

    @App
    MoVirtApp app;

    @ViewById
    DrawerLayout drawerLayout;

    @ViewById
    ViewPager viewPager;

    @ViewById
    PagerTabStrip pagerTabStrip;

    @ViewById
    ListView clusterDrawer;

    @StringArrayRes(R.array.main_pager_titles)
    String[] PAGER_TITLES;

    @Bean
    OVirtClient client;

    @Bean
    AccountPropertiesManager propertiesManager;

    @ViewById
    ProgressBar progress;

    @InstanceState
    String selectedClusterId;

    @InstanceState
    String selectedClusterName;

    @StringRes(R.string.all_clusters)
    String allClusters;

    private ActionBarDrawerToggle drawerToggle;
    private MatrixCursor emptyClusterCursor;
    private CursorAdapterLoader clusterAdapterLoader;

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        getSupportActionBar().setTitle(title);
    }

    @AfterViews
    void init() {

        initClusterDrawer();

        setTitle(selectedClusterName == null ? allClustersMsg : selectedClusterName);

        if (!propertiesManager.accountConfigured()) {
            showAccountDialog();
        }

        initPagers();
        setProgressBar(progress);
    }

    private void showAccountDialog() {
        String dialogTag = "accountDialog";
        DialogFragment accountDialog = new AccountDialogFragment();
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            accountDialog.show(getFragmentManager(), dialogTag);
        }
    }

    private void initPagers() {
        VmsFragment vmsFragment = new VmsFragment_();

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                vmsFragment,
                new HostsFragment_(),
                new StorageDomainFragment_(),
                new EventsFragment_());

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    private void initClusterDrawer() {
        emptyClusterCursor = new MatrixCursor(CLUSTER_PROJECTION);
        emptyClusterCursor.addRow(new String[]{allClusters, null, null, null});

        SimpleCursorAdapter clusterListAdapter = new SimpleCursorAdapter(this,
                R.layout.cluster_list_item,
                null,
                CLUSTER_PROJECTION,
                new int[]{R.id.cluster_view}, 0);

        clusterAdapterLoader = new CursorAdapterLoader(clusterListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return providerFacade.query(Cluster.class).orderBy(NAME).asLoader();
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                super.onLoadFinished(loader, new MergeCursor(new Cursor[]{emptyClusterCursor, data}));
            }
        };

        clusterListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex != 0) {
                    // do not display id column
                    return true;
                }
                TextView textView = (TextView) view;
                String clusterName = cursor.getString(cursor.getColumnIndex(NAME)); // only names selected, thus only 1 column
                textView.setText(clusterName);

                return true;
            }
        });
        clusterDrawer.setAdapter(clusterListAdapter);

        getSupportLoaderManager().initLoader(CLUSTER_LOADER, null, clusterAdapterLoader);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle.syncState();
    }

    @Override
    public void restartLoader() {
        super.restartLoader();
        getSupportLoaderManager().restartLoader(CLUSTER_LOADER, null, clusterAdapterLoader);
    }

    @Override
    public void destroyLoader() {
        super.destroyLoader();
        getSupportLoaderManager().destroyLoader(CLUSTER_LOADER);
    }

    @OptionsItem(R.id.action_dashboard)
    void showDashboard() {
        startActivity(new Intent(this, DashboardActivity_.class));
    }

    @OptionsItem(R.id.action_settings)
    void showSettings() {
        startActivity(new Intent(this, SettingsActivity_.class));
    }

    @OptionsItem(R.id.action_camera)
    void openCamera() {
        final Intent intent = new Intent(this, CameraActivity_.class);
        startActivity(intent);
    }

    @OptionsItem(R.id.action_edit_triggers)
    void editTriggers() {
        final Intent intent = new Intent(this, EditTriggersActivity_.class);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_ID, selectedClusterId);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_NAME, selectedClusterName);
        intent.putExtra(EditTriggersActivity.EXTRA_SCOPE, selectedClusterId == null ? Trigger.Scope.GLOBAL : Trigger.Scope.CLUSTER);
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @ItemClick
    void clusterDrawerItemClicked(Cursor cursor) {
        Cluster cluster = EntityMapper.CLUSTER_MAPPER.fromCursor(cursor);
        selectCluster(cluster);
    }

    private void selectCluster(Cluster cluster) {
        Log.d(TAG, "Updating selected cluster: id=" + cluster.getId() + ", name=" + cluster.getName());
        setTitle(cluster.getId() == null ? allClustersMsg : cluster.getName());
        selectedClusterId = cluster.getId();
        selectedClusterName = cluster.getName();

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (null != fragments) {
            for (Fragment fra : fragments) {
                if (fra instanceof SelectedClusterAware && !fra.isDetached()) {
                    ((SelectedClusterAware) fra).updateSelectedClusterId(selectedClusterId);
                }
            }
        }

        drawerLayout.closeDrawers();
    }

    @Receiver(actions = Broadcasts.NO_CONNECTION_SPECIFIED, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void noConnection(@Receiver.Extra(AccountManager.KEY_INTENT) Parcelable toOpen) {
        showAccountDialog();
    }

    /**
     * Displays fragment based on intent's action
     * Sets ordering for BaseListFragment based on intent's extras
     *
     * @param intent intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        // sets ordering for BaseListFragment
        if (extras != null) {
            final MainActivityFragments fragmentPosition = (MainActivityFragments) extras.getSerializable(Extras.FRAGMENT.name());
            final String orderBy = extras.getString(Extras.ORDER_BY.name());
            final SortOrder order = (SortOrder) extras.getSerializable(Extras.ORDER.name());

            if (fragmentPosition != null && order != null && !StringUtils.isEmpty(orderBy)) {
                viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == fragmentPosition.ordinal()) {
                            Fragment fragment = getSupportFragmentManager().getFragments().get(fragmentPosition.ordinal());

                            if (fragment != null && fragment instanceof BaseListFragment && !fragment.isDetached()) {
                                BaseListFragment baseListFragment = (BaseListFragment) fragment;
                                baseListFragment.setOrderingSpinners(orderBy, order);
                            }
                        }
                        viewPager.setOnPageChangeListener(null);
                    }

                    @Override
                    public void onPageSelected(int position) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
            }
        }

        String action = intent.getAction();
        if (action != null && !action.isEmpty()) {
            viewPager.setCurrentItem(MainActivityFragments.valueOf(intent.getAction()).ordinal(), false);
        }
    }

    public enum Extras {
        ORDER,
        ORDER_BY,
        FRAGMENT
    }
}
