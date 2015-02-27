package org.ovirt.mobile.movirt.ui;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.InstanceState;
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
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity implements ClusterDrawerFragment.ClusterSelectedListener, TabChangedListener.HasCurrentlyShown {

    private static final String TAG = MainActivity.class.getSimpleName();

    Dialog connectionNotConfiguredProperlyDialog;

    @StringRes(R.string.needs_configuration)
    String noAccMsg;

    @StringRes(R.string.connection_not_correct)
    String accIncorrectMsg;

    @App
    MoVirtApp app;

    @ViewById
    DrawerLayout drawerLayout;

    @FragmentById
    EventsFragment eventList;

    @FragmentById
    VmsFragment vmsList;

    @ViewById
    View vmsLayout;

    @ViewById
    View eventsLayout;

    @FragmentById
    ClusterDrawerFragment clusterDrawer;

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

        getActionBar().setTitle(title);
    }

    @AfterViews
    void initAdapters() {

        connectionNotConfiguredProperlyDialog = new Dialog(this);


        clusterDrawer.initDrawerLayout(drawerLayout);
        clusterDrawer.getDrawerToggle().syncState();

        onClusterSelected(new Cluster() {{ setId(selectedClusterId); setName(selectedClusterName); }});

        if (!authenticator.accountConfigured()) {
            showDialogToOpenAccountSettings(noAccMsg, new Intent(this, AuthenticatorActivity_.class));
        }

        initTabs();
    }

    private void initTabs() {
        vmsLayout.setVisibility(currentlyShown == TabChangedListener.CurrentlyShown.VMS ? View.VISIBLE : View.GONE);
        eventsLayout.setVisibility(currentlyShown == TabChangedListener.CurrentlyShown.EVENTS ? View.VISIBLE : View.GONE);

        TabChangedListener.CurrentlyShown tmpCurrentlyShown = currentlyShown;

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab vmsTab = getActionBar().newTab()
                .setText("VMs")
                .setTabListener(new TabChangedListener(vmsLayout, TabChangedListener.CurrentlyShown.VMS, this));

        getActionBar().addTab(vmsTab);

        ActionBar.Tab eventsTab = getActionBar().newTab()
                .setText("Events")
                .setTabListener(new TabChangedListener(eventsLayout, TabChangedListener.CurrentlyShown.EVENTS, this));

        getActionBar().addTab(eventsTab);

        if (tmpCurrentlyShown == TabChangedListener.CurrentlyShown.EVENTS) {
            eventsTab.select();
        } else {
            vmsTab.select();
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (clusterDrawer.getDrawerToggle().onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClusterSelected(Cluster cluster) {
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
