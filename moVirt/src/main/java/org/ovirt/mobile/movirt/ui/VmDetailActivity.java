package org.ovirt.mobile.movirt.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.VmStatistics;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.ActionTicket;
import org.ovirt.mobile.movirt.rest.ExtendedVm;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

@EActivity(R.layout.activity_vm_detail)
@OptionsMenu(R.menu.vm)
public class VmDetailActivity extends Activity implements TabChangedListener.HasCurrentlyShown {

    private static final String TAG = VmDetailActivity.class.getSimpleName();

    private String vmId = null;

    @InstanceState
    TabChangedListener.CurrentlyShown currentlyShown = TabChangedListener.CurrentlyShown.VM_DETAIL_GENERAL;

    @ViewById
    View generalFragment;

    @ViewById
    View disksFragment;

    @ViewById
    View eventsFragment;

    @FragmentById
    DiskDetailFragment diskDetails;

    @FragmentById
    EventsFragment eventsList;

    @AfterViews
    void init() {
        Uri vmUri = getIntent().getData();
        vmId = vmUri.getLastPathSegment();

        diskDetails.setVmId(vmId);
        eventsList.setFilterVmId(vmId);

        initTabs();
    }

    private void initTabs() {
        generalFragment.setVisibility(currentlyShown == TabChangedListener.CurrentlyShown.VM_DETAIL_GENERAL ? View.VISIBLE : View.GONE);
        eventsFragment.setVisibility(currentlyShown == TabChangedListener.CurrentlyShown.EVENTS ? View.VISIBLE : View.GONE);
        disksFragment.setVisibility(currentlyShown == TabChangedListener.CurrentlyShown.DISKS ? View.VISIBLE : View.GONE);

        TabChangedListener.CurrentlyShown tmpCurrentlyShown = currentlyShown;

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab generalTab = getActionBar().newTab()
                .setText("General")
                .setTabListener(new TabChangedListener(generalFragment, TabChangedListener.CurrentlyShown.VM_DETAIL_GENERAL, this));

        ActionBar.Tab eventsTab = getActionBar().newTab()
                .setText("Events")
                .setTabListener(new TabChangedListener(eventsFragment, TabChangedListener.CurrentlyShown.EVENTS, this));

        ActionBar.Tab disksTab = getActionBar().newTab()
                .setText("Disks")
                .setTabListener(new TabChangedListener(disksFragment, TabChangedListener.CurrentlyShown.DISKS, this));

        getActionBar().addTab(generalTab);
        getActionBar().addTab(disksTab);
        getActionBar().addTab(eventsTab);

        if (tmpCurrentlyShown == TabChangedListener.CurrentlyShown.EVENTS) {
            eventsTab.select();
        } else if (tmpCurrentlyShown == TabChangedListener.CurrentlyShown.DISKS) {
            disksTab.select();
        } else {
            generalTab.select();
        }

    }

    @Receiver(actions = Broadcasts.CONNECTION_FAILURE, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void connectionFailure(@Receiver.Extra(Broadcasts.Extras.CONNECTION_FAILURE_REASON) String reason) {
        Toast.makeText(VmDetailActivity.this, R.string.rest_req_failed + " " + reason, Toast.LENGTH_LONG).show();
    }

    @OptionsItem(R.id.action_edit_triggers)
    void editTriggers() {
        final Intent intent = new Intent(this, EditTriggersActivity_.class);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_ID, vmId);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_NAME, vmId);
        intent.putExtra(EditTriggersActivity.EXTRA_SCOPE, Trigger.Scope.ITEM);
        startActivity(intent);
    }

    @Override
    public void setCurrentlyShown(TabChangedListener.CurrentlyShown currentlyShown) {
        this.currentlyShown = currentlyShown;
    }
}
