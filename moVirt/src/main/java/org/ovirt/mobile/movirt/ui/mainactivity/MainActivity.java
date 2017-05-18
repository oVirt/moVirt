package org.ovirt.mobile.movirt.ui.mainactivity;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.CameraActivity_;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.MainSettingsActivity_;
import org.ovirt.mobile.movirt.ui.PresenterStatusSyncableActivity;
import org.ovirt.mobile.movirt.ui.SettingsActivity_;
import org.ovirt.mobile.movirt.ui.account.EditAccountsActivity_;
import org.ovirt.mobile.movirt.ui.dashboard.DashboardActivity_;
import org.ovirt.mobile.movirt.ui.dialogs.AccountDialogFragment;
import org.ovirt.mobile.movirt.ui.events.EventsFragment_;
import org.ovirt.mobile.movirt.ui.hosts.HostsFragment_;
import org.ovirt.mobile.movirt.ui.listfragment.BaseListFragment;
import org.ovirt.mobile.movirt.ui.storage.StorageDomainFragment_;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment_;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends PresenterStatusSyncableActivity implements MainContract.View {

    @ViewById
    DrawerLayout drawerLayout;

    @ViewById
    ViewPager viewPager;

    @ViewById
    PagerTabStrip pagerTabStrip;

    @ViewById
    LinearLayout selectionDrawer;

    @StringArrayRes(R.array.main_pager_titles)
    String[] PAGER_TITLES;

    @ViewById
    ProgressBar progress;

    private ActionBarDrawerToggle drawerToggle;

    private TreeNode drawerRoot;

    private MainContract.Presenter presenter;

    @AfterViews
    void init() {

        initClusterDrawer();
        initPagers();
        setProgressBar(progress);

        presenter = MainPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .initialize();
    }

    @Override
    public MainContract.Presenter getPresenter() {
        return presenter;
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
    public void displayTitle(String title) {
        setTitle(title);
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void showAccountDialog() {
        String dialogTag = "accountDialog";
        DialogFragment accountDialog = new AccountDialogFragment();
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            accountDialog.show(getFragmentManager(), dialogTag);
        }
    }

    @Override
    public void showAccountsAndClusters(SortedMap<MovirtAccount, List<Cluster>> map) {
        AccountsTreeItem.ActiveSelectionChangedListener changedListener = presenter::onActiveSelectionChanged;
        AccountsTreeItem.LongClickListener longClickListener = presenter::onLongClickListener;

        TreeNode anchor = TreeNode.root();
        AccountsTreeItem rootData = new AccountsTreeItem(null, null, changedListener, longClickListener);
        drawerRoot = new TreeNode(rootData)
                .setViewHolder(new AccountsTreeHolder(this))
                .setExpanded(true);
        anchor.addChild(drawerRoot);

        for (Map.Entry<MovirtAccount, List<Cluster>> entry : map.entrySet()) {
            AccountsTreeItem accountData = new AccountsTreeItem(entry.getKey(), null, changedListener, longClickListener);
            TreeNode accountNode = new TreeNode(accountData)
                    .setViewHolder(new AccountsTreeHolder(this))
                    .setExpanded(true);
            drawerRoot.addChild(accountNode);

            for (Cluster cluster : entry.getValue()) {
                AccountsTreeItem clusterData = new AccountsTreeItem(entry.getKey(), cluster, changedListener, longClickListener);
                TreeNode newClusterNode = new TreeNode(clusterData)
                        .setViewHolder(new AccountsTreeHolder(this))
                        .setExpanded(true);
                accountNode.addChild(newClusterNode);
            }
        }

        AndroidTreeView atv = new AndroidTreeView(this, anchor);
        atv.setUseAutoToggle(false);
        atv.setSelectionModeEnabled(true);
        atv.setDefaultContainerStyle(R.style.LargePaddingTreeNode);
        selectionDrawer.removeAllViews(); // remove previous tree
        selectionDrawer.addView(atv.getView());
    }

    @Override
    public void selectActiveSelection(ActiveSelection activeSelection) {
        if (drawerRoot != null) {
            if (possiblySelect(drawerRoot, activeSelection)) {
                return;
            }

            for (TreeNode account : drawerRoot.getChildren()) {
                if (possiblySelect(account, activeSelection)) {
                    return;
                }

                for (TreeNode cluster : account.getChildren()) {
                    if (possiblySelect(cluster, activeSelection)) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * @return true if success
     */
    private boolean possiblySelect(TreeNode node, ActiveSelection activeSelection) {
        ActiveSelection possible = ((AccountsTreeHolder) node.getViewHolder())
                .<AccountsTreeItem>getDataNode().asActiveSelection();

        if (possible.equals(activeSelection)) {
            ((AccountsTreeHolder) node.getViewHolder()).selectNode();
            return true;
        }
        return false;
    }

    @Override
    public void startEditAccountsActivity() {
        startActivity(new Intent(getApplicationContext(), EditAccountsActivity_.class));
    }

    @Override
    public void startAccountSettingsActivity(MovirtAccount account) {
        final Intent intent = new Intent(this, SettingsActivity_.class);
        intent.putExtra(Constants.ACCOUNT_KEY, account);
        startActivity(intent);
    }

    @OptionsItem(R.id.action_dashboard)
    void showDashboard() {
        startActivity(new Intent(this, DashboardActivity_.class));
    }

    @OptionsItem(R.id.action_settings)
    void showSettings() {
        startActivity(new Intent(this, MainSettingsActivity_.class));
    }

    @OptionsItem(R.id.action_camera)
    void openCamera() {
        startActivity(new Intent(this, CameraActivity_.class));
    }

    @OptionsItem(R.id.action_edit_triggers)
    void editTriggers() {
        startActivity(new Intent(this, EditTriggersActivity_.class));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void hideDrawer() {
        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
