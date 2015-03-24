package org.ovirt.mobile.movirt.ui;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;

import java.util.ArrayList;
import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.NamedEntity.NAME;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends ActionBarActivity implements TabChangedListener.HasCurrentlyShown {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_ACTIVE_TAB = "org.ovirt.mobile.movirt.ACTIVE_TAB";

    Dialog connectionNotConfiguredProperlyDialog;

    @StringRes(R.string.needs_configuration)
    String noAccMsg;

    @StringRes(R.string.connection_not_correct)
    String accIncorrectMsg;

    @App
    MoVirtApp app;

    @Bean
    ProviderFacade provider;

    @ViewById
    DrawerLayout drawerLayout;

    @ViewById
    ViewPager viewPager;

    @ViewById
    ListView clusterDrawer;

    @StringRes(R.string.cluster_scope)
    String CLUSTER_SCOPE;

    @Bean
    OVirtClient client;

    @InstanceState
    String selectedClusterId;

    @InstanceState
    String selectedClusterName;

    @InstanceState
    TabChangedListener.CurrentlyShown currentlyShown = TabChangedListener.CurrentlyShown.VMS;

    @Bean
    SyncUtils syncUtils;

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    EventsHandler eventsHandler;

    private ActionBarDrawerToggle drawerToggle;

    EventsFragment eventList;

    VmsFragment vmsList;

    @Override
    protected void onPause() {
        super.onPause();
        if (connectionNotConfiguredProperlyDialog.isShowing()) {
            connectionNotConfiguredProperlyDialog.dismiss();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        getSupportActionBar().setTitle(title);
    }

    @AfterViews
    void init() {
        Intent intent = getIntent();
        String sTabFromIntent = intent.getStringExtra(EXTRA_ACTIVE_TAB);
        if (sTabFromIntent != null) {
            TabChangedListener.CurrentlyShown tabFromIntent =
                    TabChangedListener.CurrentlyShown.valueOf(sTabFromIntent);
            setCurrentlyShown(tabFromIntent);
        }
        connectionNotConfiguredProperlyDialog = new Dialog(this);

        initClusterDrawer();

        selectCluster(new Cluster(selectedClusterId, selectedClusterName));

        if (!authenticator.accountConfigured()) {
            showDialogToOpenAccountSettings(noAccMsg, new Intent(this, AuthenticatorActivity_.class));
        }

        initPagers();
    }

    private void initPagers() {
        final List<View> viewList = new ArrayList<>();
        LayoutInflater inflater = getLayoutInflater();
        View vmsView = inflater.inflate(R.layout.fragment_vms, null);
        vmsList = (VmsFragment)getSupportFragmentManager().findFragmentById(R.id.vmsList);
        View eventsView = inflater.inflate(R.layout.fragment_events, null);
        eventList = (EventsFragment)getSupportFragmentManager().findFragmentById(R.id.eventList);
        viewList.add(vmsView);
        viewList.add(eventsView);

        final List<String> viewTitleList = new ArrayList<>();
        viewTitleList.add("VMs");
        viewTitleList.add("Events");

        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return viewTitleList.get(position);
            }
        };

        viewPager.setAdapter(pagerAdapter);

    }

    @StringRes(R.string.all_clusters)
    String allClusters;

    private static final String[] CLUSTER_PROJECTION = new String[] {OVirtContract.Cluster.NAME, OVirtContract.Cluster.ID};
    private MatrixCursor emptyClusterCursor;

    private void initClusterDrawer() {
        emptyClusterCursor = new MatrixCursor(CLUSTER_PROJECTION);
        emptyClusterCursor.addRow(new String[]{allClusters, null});


        SimpleCursorAdapter clusterListAdapter = new SimpleCursorAdapter(this,
                R.layout.cluster_list_item,
                null,
                CLUSTER_PROJECTION,
                new int[]{R.id.cluster_view}, 0);

        CursorAdapterLoader clusterAdapterLoader = new CursorAdapterLoader(clusterListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return provider.query(Cluster.class).orderBy(NAME).asLoader();
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

        getSupportLoaderManager().initLoader(0, null, clusterAdapterLoader);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerLayout.setDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle.syncState();

    }

    private void showDialogToOpenAccountSettings(String msg, final Intent intent) {
        if (connectionNotConfiguredProperlyDialog.isShowing()) {
            return;
        }

        connectionNotConfiguredProperlyDialog.setContentView(R.layout.settings_dialog);
        connectionNotConfiguredProperlyDialog.setTitle(getString(R.string.configuration));

        TextView label = (TextView) connectionNotConfiguredProperlyDialog.findViewById(R.id.text);
        label.setText(msg);

        Button continueButton = (Button) connectionNotConfiguredProperlyDialog.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionNotConfiguredProperlyDialog.dismiss();
                startActivity(intent);
            }
        });

        Button ignoreButton = (Button) connectionNotConfiguredProperlyDialog.findViewById(R.id.ignoreButton);
        ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionNotConfiguredProperlyDialog.dismiss();
            }
        });

        connectionNotConfiguredProperlyDialog.show();
    }

    @OptionsItem(R.id.action_refresh)
    @Background
    public void onRefresh() {
        Log.d(TAG, "Refresh button clicked");
        syncUtils.triggerRefresh();
    }

    @OptionsItem(R.id.action_settings)
    void showSettings() {
        startActivity(new Intent(this, SettingsActivity_.class));
    }

    @OptionsItem(R.id.action_clear_events)
    void clearEvents() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        eventsHandler.deleteEvents();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all the events stored locally?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
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
        setTitle(cluster.getId() == null ? getString(R.string.all_clusters) : String.format(CLUSTER_SCOPE, cluster.getName()));
        selectedClusterId = cluster.getId();
        selectedClusterName = cluster.getName();


        if (eventList != null) {
            eventList.updateFilterClusterIdTo(selectedClusterId);
        }

        if (vmsList != null) {
            vmsList.updateFilterClusterIdTo(selectedClusterId);
        }

        drawerLayout.closeDrawers();
    }


    @Receiver(actions = Broadcasts.NO_CONNECTION_SPEFICIED, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void noConnection(@Receiver.Extra(AccountManager.KEY_INTENT) Parcelable toOpen) {
        showDialogToOpenAccountSettings(accIncorrectMsg, (Intent) toOpen);
    }

    @Receiver(actions = Broadcasts.CONNECTION_FAILURE, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void connectionFailure(@Receiver.Extra(Broadcasts.Extras.CONNECTION_FAILURE_REASON) String reason) {
        Toast.makeText(MainActivity.this, R.string.rest_req_failed + " " + reason, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setCurrentlyShown(TabChangedListener.CurrentlyShown currentlyShown) {
        this.currentlyShown = currentlyShown;
    }
}
