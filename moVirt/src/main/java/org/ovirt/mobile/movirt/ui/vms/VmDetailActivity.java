package org.ovirt.mobile.movirt.ui.vms;

import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.rest.ActionTicket;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.DiskDetailFragment;
import org.ovirt.mobile.movirt.ui.DiskDetailFragment_;
import org.ovirt.mobile.movirt.ui.EventsFragment;
import org.ovirt.mobile.movirt.ui.EventsFragment_;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MoVirtActivity;
import org.ovirt.mobile.movirt.ui.NicDetailFragment;
import org.ovirt.mobile.movirt.ui.NicDetailFragment_;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.UpdateMenuItemAware;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

@EActivity(R.layout.activity_vm_detail)
@OptionsMenu(R.menu.vm)
public class VmDetailActivity extends MoVirtActivity implements HasProgressBar, UpdateMenuItemAware<Vm> {

    private static final String TAG = VmDetailActivity.class.getSimpleName();
    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringArrayRes(R.array.vm_detail_pager_titles)
    String[] PAGER_TITLES;
    @Bean
    OVirtClient client;
    @ViewById
    ProgressBar progress;
    @Bean
    VmFacade vmFacade;
    @OptionsMenuItem(R.id.action_run)
    MenuItem menuRun;
    @OptionsMenuItem(R.id.action_stop)
    MenuItem menuStop;
    @OptionsMenuItem(R.id.action_reboot)
    MenuItem menuReboot;
    @OptionsMenuItem(R.id.action_console)
    MenuItem menuConsole;
    private String vmId = null;

    @AfterViews
    void init() {
        Uri vmUri = getIntent().getData();
        vmId = vmUri.getLastPathSegment();

        initPagers();
        hideProgressBar();
    }

    private void initPagers() {
        EventsFragment eventsList = new EventsFragment_();
        DiskDetailFragment diskDetails = new DiskDetailFragment_();
        NicDetailFragment nicDetails = new NicDetailFragment_();

        eventsList.setFilterVmId(vmId);
        diskDetails.setVmId(vmId);
        nicDetails.setVmId(vmId);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new VmDetailGeneralFragment_(),
                eventsList,
                diskDetails,
                nicDetails);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
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

    @OptionsItem(R.id.action_run)
    @Background
    void start() {
        client.startVm(vmId);
        syncVm();
    }

    @OptionsItem(R.id.action_stop)
    @Background
    void stop() {
        client.stopVm(vmId);
        syncVm();
    }

    @OptionsItem(R.id.action_reboot)
    @Background
    void reboot() {
        client.rebootVm(vmId);
        syncVm();
    }

    @OptionsItem(R.id.action_console)
    @Background
    void openConsole() {
        vmFacade.sync(vmId, new ProgressBarResponse<Vm>(this) {

            @Override
            public void onResponse(final Vm freshVm) throws RemoteException {

                client.getConsoleTicket(vmId, new ProgressBarResponse<ActionTicket>(VmDetailActivity.this) {
                    @Override
                    public void onResponse(ActionTicket ticket) throws RemoteException {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW)
                                    .setType("application/vnd.vnc")
                                    .setData(Uri.parse(makeConsoleUrl(freshVm, ticket)));
                            startActivity(intent);
                        } catch (IllegalArgumentException e) {
                            makeToast(e.getMessage());
                        } catch (Exception e) {
                            makeToast("Failed to open console client. Check if aSPICE/bVNC is installed.");
                        }
                    }
                });
            }
        });
    }

    private void syncVm() {
        vmFacade.sync(vmId, new ProgressBarResponse<Vm>(this));
    }

    /**
     * Returns URL for running console intent.
     *
     * @throws java.lang.IllegalArgumentException with description
     *                                            if the URL can't be created from input.
     */
    private String makeConsoleUrl(Vm vm, ActionTicket ticket)
            throws IllegalArgumentException {

        if (vm.getDisplayType() == null) {
            throw new IllegalArgumentException("Vm's display type cannot be null");
        }

        String passwordPart = "";
        if (ticket != null && ticket.ticket != null && ticket.ticket.value != null
                && !ticket.ticket.value.isEmpty()) {
            switch (vm.getDisplayType()) {
                case VNC:
                    passwordPart = "VncPassword";
                    break;
                case SPICE:
                    passwordPart = "SpicePassword";
                    break;
            }
            passwordPart += "=" + ticket.ticket.value;
        }

        return vm.getDisplayType().getProtocol() + "://" + vm.getDisplayAddress() + ":" + vm.getDisplayPort() + "?" + passwordPart;
    }

    @UiThread
    void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @UiThread
    @Override
    public void showProgressBar() {
        progress.setVisibility(View.VISIBLE);
    }

    @UiThread
    @Override
    public void hideProgressBar() {
        progress.setVisibility(View.GONE);
    }

    @UiThread
    @Override
    public void updateMenuItem(Vm vm) {
        if (menuRun == null || menuStop == null || menuReboot == null || menuConsole == null)
            return;

        menuRun.setVisible(Vm.Command.RUN.canExecute(vm.getStatus()));
        menuStop.setVisible(Vm.Command.STOP.canExecute(vm.getStatus()));
        menuReboot.setVisible(Vm.Command.REBOOT.canExecute(vm.getStatus()));
        menuConsole.setVisible(Vm.Command.CONSOLE.canExecute(vm.getStatus()));
    }
}
