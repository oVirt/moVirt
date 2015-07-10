package org.ovirt.mobile.movirt.ui.vms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.ActionTicket;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.AdvancedAuthenticatorActivity;
import org.ovirt.mobile.movirt.ui.AdvancedAuthenticatorActivity_;
import org.ovirt.mobile.movirt.ui.Constants;
import org.ovirt.mobile.movirt.ui.DiskDetailFragment;
import org.ovirt.mobile.movirt.ui.DiskDetailFragment_;
import org.ovirt.mobile.movirt.ui.EventsFragment;
import org.ovirt.mobile.movirt.ui.EventsFragment_;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;
import org.ovirt.mobile.movirt.ui.NicDetailFragment;
import org.ovirt.mobile.movirt.ui.NicDetailFragment_;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.UpdateMenuItemAware;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

import java.io.File;

@EActivity(R.layout.activity_vm_detail)
@OptionsMenu(R.menu.vm)
public class VmDetailActivity extends MovirtActivity implements HasProgressBar, UpdateMenuItemAware<Vm> {

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

    @Bean
    ProviderFacade providerFacade;

    @Bean
    MovirtAuthenticator authenticator;
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
        setProgressBar(progress);
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
                            if (freshVm.getDisplayType() == Vm.Display.SPICE && freshVm.getDisplaySecurePort() != -1 && !isCaFileExists()) {
                                showMissingCaCertDialog();
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW)
                                        .setType("application/vnd.vnc")
                                        .setData(Uri.parse(makeConsoleUrl(freshVm, ticket)));
                                startActivity(intent);
                            }
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

    @UiThread
    void showMissingCaCertDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.missing_ca_cert)
                .setPositiveButton(R.string.import_str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(VmDetailActivity.this, AdvancedAuthenticatorActivity_.class);
                        intent.putExtra(AdvancedAuthenticatorActivity.MODE, AdvancedAuthenticatorActivity.MODE_SPICE_CA_MANAGEMENT);
                        intent.putExtra(AdvancedAuthenticatorActivity.ENFORCE_HTTP_BASIC_AUTH, authenticator.enforceBasicAuth());
                        intent.putExtra(AdvancedAuthenticatorActivity.CERT_HANDLING_STRATEGY, authenticator.getCertHandlingStrategy());
                        intent.putExtra(AdvancedAuthenticatorActivity.LOAD_CA_FROM, authenticator.getApiUrl());
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makeToast(getString(R.string.can_not_run_console_without_ca));
                    }
                })
                .show();
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

        String parameters = "";
        if (ticket != null && ticket.ticket != null && ticket.ticket.value != null
                && !ticket.ticket.value.isEmpty()) {
            switch (vm.getDisplayType()) {
                case VNC:
                    String vncPasswordPart = Constants.PARAM_VNC_PWD + "=" + ticket.ticket.value;
                    parameters = vncPasswordPart;
                    break;
                case SPICE:
                    String spicePasswordPart = Constants.PARAM_SPICE_PWD + "=" + ticket.ticket.value;
                    parameters = spicePasswordPart;
                    if (vm.getDisplaySecurePort() != -1) {
                        String caCertPath = Constants.getCaCertPath(this);
                        String tlsPortPart = Constants.PARAM_TLS_PORT + "=" + vm.getDisplaySecurePort();
                        String certSubjectPart = Constants.PARAM_CERT_SUBJECT + "=" + vm.getCertificateSubject();
                        String caCertPathPart = Constants.PARAM_CA_CERT_PATH + "=" + caCertPath;

                        parameters += "&" + tlsPortPart + "&" + certSubjectPart + "&" + caCertPathPart;
                    }
                    break;
            }
        }

        String url = vm.getDisplayType().getProtocol() + "://" + vm.getDisplayAddress() + ":" + vm.getDisplayPort()
                + "?" + parameters;
        return url;
    }

    private boolean isCaFileExists() {
        File file = new File(Constants.getCaCertPath(this));
        return file.exists();
    }

    @UiThread
    void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
