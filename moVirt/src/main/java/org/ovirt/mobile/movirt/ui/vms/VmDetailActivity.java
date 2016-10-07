package org.ovirt.mobile.movirt.ui.vms;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.facade.ConsoleFacade;
import org.ovirt.mobile.movirt.facade.SnapshotFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Console;
import org.ovirt.mobile.movirt.model.Display;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.ActionTicket;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.Constants;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;
import org.ovirt.mobile.movirt.ui.NewSnapshotListener;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.CreateSnapshotDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.CreateSnapshotDialogFragment_;
import org.ovirt.mobile.movirt.ui.dialogs.ImportCertificateDialogFragment;
import org.ovirt.mobile.movirt.ui.events.EventsFragment;
import org.ovirt.mobile.movirt.ui.events.EventsFragment_;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

import java.io.File;
import java.util.List;
import java.util.Set;

@EActivity(R.layout.activity_vm_detail)
@OptionsMenu(R.menu.vm)
public class VmDetailActivity extends MovirtActivity implements HasProgressBar,
        ConfirmDialogFragment.ConfirmDialogListener, NewSnapshotListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = VmDetailActivity.class.getSimpleName();
    private static final int REQUEST_MIGRATE = 0;
    private static final int ACTION_STOP_VM = 0;
    private static final int ACTION_REBOOT_VM = 1;
    private static final int ACTION_STOP_MIGRATE_VM = 2;

    private static final int SNAPSHOTS_LOADER = 1; // 0 in MovirtActivity
    private static final int VMS_LOADER = 2;
    private static final int CONSOLES_LOADER = 3;

    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringRes(R.string.details_for_vm)
    String VM_DETAILS;
    @StringArrayRes(R.array.vm_detail_pager_titles)
    String[] PAGER_TITLES;
    @Bean
    OVirtClient client;
    @Bean
    MovirtAuthenticator movirtAuthenticator;
    @Bean
    ProviderFacade provider;
    @ViewById
    ProgressBar progress;
    @Bean
    VmFacade vmFacade;
    @Bean
    ConsoleFacade consoleFacade;
    @Bean
    SnapshotFacade snapshotFacade;
    @App
    MoVirtApp app;
    @OptionsMenuItem(R.id.action_run)
    MenuItem menuRun;
    @OptionsMenuItem(R.id.action_stop)
    MenuItem menuStop;
    @OptionsMenuItem(R.id.action_reboot)
    MenuItem menuReboot;
    @OptionsMenuItem(R.id.action_start_migration)
    MenuItem menuStartMigration;
    @OptionsMenuItem(R.id.action_cancel_migration)
    MenuItem menuCancelMigration;
    @OptionsMenuItem(R.id.action_spice_console)
    MenuItem menuSpiceConsole;
    @OptionsMenuItem(R.id.action_vnc_console)
    MenuItem menuVncConsole;
    @OptionsMenuItem(R.id.action_create_snapshot)
    MenuItem menuCreateSnapshot;

    private String vmId = null;
    private Vm currentVm = null;
    private boolean menuCreateSnapshotVisibility = false;
    private boolean hasSpiceConsole = false;
    private boolean hasVncConsole = false;
    private List<Console> consoles = null;

    @AfterViews
    void init() {
        Uri vmUri = getIntent().getData();
        vmId = vmUri.getLastPathSegment();

        initPagers();
        initLoaders();
        setProgressBar(progress);
    }

    private void initPagers() {
        EventsFragment eventList = new EventsFragment_();
        VmDisksFragment diskList = new VmDisksFragment_();
        VmNicsFragment nicList = new VmNicsFragment_();
        VmSnapshotsFragment snapshotList = new VmSnapshotsFragment_();

        eventList.setFilterVmId(vmId);
        diskList.setFilterVmId(vmId);
        diskList.setFilterSnapshotId("");
        nicList.setFilterVmId(vmId);
        nicList.setFilterSnapshotId("");
        snapshotList.setFilterVmId(vmId);
        snapshotList.addOrdering(OVirtContract.Snapshot.SNAPSHOT_STATUS);
        snapshotList.addOrdering(OVirtContract.Snapshot.TYPE);
        snapshotList.addOrdering(OVirtContract.Snapshot.NAME);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new VmDetailGeneralFragment_(),
                eventList,
                snapshotList,
                diskList,
                nicList);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
        refreshConsoles();
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(SNAPSHOTS_LOADER, null, this);
        getSupportLoaderManager().initLoader(VMS_LOADER, null, this);
        if (movirtAuthenticator.isV4Api()) {
            getSupportLoaderManager().initLoader(CONSOLES_LOADER, null, this);
        }
    }

    @Override
    public void restartLoader() {
        super.restartLoader();
        getSupportLoaderManager().restartLoader(SNAPSHOTS_LOADER, null, this);
        getSupportLoaderManager().restartLoader(VMS_LOADER, null, this);
        if (movirtAuthenticator.isV4Api()) {
            getSupportLoaderManager().restartLoader(CONSOLES_LOADER, null, this);
        }
    }

    @Override
    public void destroyLoader() {
        super.destroyLoader();
        getSupportLoaderManager().destroyLoader(SNAPSHOTS_LOADER);
        getSupportLoaderManager().destroyLoader(VMS_LOADER);
        if (movirtAuthenticator.isV4Api()) {
            getSupportLoaderManager().destroyLoader(CONSOLES_LOADER);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;

        switch (id) {
            case SNAPSHOTS_LOADER:
                loader = provider.query(Snapshot.class).where(OVirtContract.Snapshot.VM_ID, vmId).asLoader();
                break;
            case VMS_LOADER:
                loader = provider.query(Vm.class).id(vmId).asLoader();
                break;
            case CONSOLES_LOADER:
                if (movirtAuthenticator.isV4Api()) {
                    loader = provider.query(Console.class).where(OVirtContract.Console.VM_ID, vmId).asLoader();
                }
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading data: id=" + loader.getId());
            return;
        }

        switch (loader.getId()) {
            case SNAPSHOTS_LOADER:
                List<Snapshot> snapshots = snapshotFacade.mapAllFromCursor(data);
                menuCreateSnapshotVisibility = !Snapshot.containsOneOfStatuses(snapshots, Snapshot.SnapshotStatus.LOCKED, Snapshot.SnapshotStatus.IN_PREVIEW);
                invalidateOptionsMenu();
                break;
            case VMS_LOADER:
                currentVm = vmFacade.mapFromCursor(data);
                invalidateOptionsMenu();
                break;
            case CONSOLES_LOADER:
                if (movirtAuthenticator.isV4Api()) {
                    consoles = consoleFacade.mapAllFromCursor(data);
                    Set<Display> displays = Display.getDisplayTypes(consoles);
                    hasSpiceConsole = displays.contains(Display.SPICE);
                    hasVncConsole = displays.contains(Display.VNC);
                }
                invalidateOptionsMenu();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentVm != null) {
            setTitle(String.format(VM_DETAILS, currentVm.getName()));
            Vm.Status status = currentVm.getStatus();
            menuRun.setVisible(Vm.Command.RUN.canExecute(status));
            menuStop.setVisible(Vm.Command.STOP.canExecute(status));
            menuReboot.setVisible(Vm.Command.REBOOT.canExecute(status));
            menuStartMigration.setVisible(Vm.Command.START_MIGRATION.canExecute(status));
            menuCancelMigration.setVisible(Vm.Command.CANCEL_MIGRATION.canExecute(status));
            menuCreateSnapshot.setVisible(menuCreateSnapshotVisibility);
            if (movirtAuthenticator.isV3Api()) {
                hasSpiceConsole = currentVm.getDisplayType() == Display.SPICE;
                hasVncConsole = currentVm.getDisplayType() == Display.VNC;
            }
            boolean consoleExecutable = Vm.Command.CONSOLE.canExecute(status);
            menuSpiceConsole.setVisible(consoleExecutable && hasSpiceConsole);
            menuVncConsole.setVisible(consoleExecutable && hasVncConsole);
        }

        return super.onPrepareOptionsMenu(menu);
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
        client.startVm(vmId, new SyncVmResponse());
    }

    @OptionsItem(R.id.action_stop)
    void stop() {
        DialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(ACTION_STOP_VM, getString(R.string.dialog_action_stop_vm));
        confirmDialog.show(getFragmentManager(), "confirmStopVM");
    }

    @OptionsItem(R.id.action_reboot)
    void reboot() {
        DialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(ACTION_REBOOT_VM, getString(R.string.dialog_action_reboot_vm));
        confirmDialog.show(getFragmentManager(), "confirmRebootVM");
    }

    @OptionsItem(R.id.action_cancel_migration)
    void cancelMigration() {
        DialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(ACTION_STOP_MIGRATE_VM,
                        getString(R.string.dialog_action_stop_migrate_vm));
        confirmDialog.show(getFragmentManager(), "confirmStopMigrateVM");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (dialogButton == DialogInterface.BUTTON_POSITIVE) {
            switch (actionId) {
                case ACTION_STOP_VM:
                    doStop();
                    break;
                case ACTION_REBOOT_VM:
                    doReboot();
                    break;
                case ACTION_STOP_MIGRATE_VM:
                    doCancelMigration();
                    break;
            }
        }
    }

    @Background
    void doStop() {
        client.stopVm(vmId, new SyncVmResponse());
    }

    @Background
    void doReboot() {
        client.rebootVm(vmId, new SyncVmResponse());
    }

    @Background
    void doCancelMigration() {
        client.cancelMigration(vmId, new SyncVmResponse());
    }

    @OptionsItem(R.id.action_start_migration)
    void showMigrationDialog() {
        if (currentVm != null) {
            Intent migrateIntent = new Intent(this, VmMigrateActivity_.class);
            migrateIntent.putExtra(VmMigrateActivity.HOST_ID_EXTRA, currentVm.getHostId());
            migrateIntent.putExtra(VmMigrateActivity.CLUSTER_ID_EXTRA, currentVm.getClusterId());
            startActivityForResult(migrateIntent, REQUEST_MIGRATE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MIGRATE) {
            if (resultCode == VmMigrateActivity.RESULT_DEFAULT) {
                doMigrationToDefault();
            }
            if (resultCode == VmMigrateActivity.RESULT_SELECT) {
                doMigrationTo(data.getStringExtra(VmMigrateActivity.RESULT_HOST_ID_EXTRA));
            }
        }
    }

    @Background
    public void doMigrationToDefault() {
        client.migrateVmToDefaultHost(vmId, new SyncVmResponse());
    }

    @Background
    public void doMigrationTo(String hostId) {
        client.migrateVmToHost(vmId, hostId, new SyncVmResponse());
    }

    @OptionsItem(R.id.action_create_snapshot)
    void createSnapshot() {
        CreateSnapshotDialogFragment dialog = new CreateSnapshotDialogFragment_();
        dialog.setVmId(vmId);
        dialog.show(getFragmentManager(), "createSnapshot");
    }

    @Override
    @Background
    public void onDialogResult(org.ovirt.mobile.movirt.rest.Snapshot snapshot) {
        client.createSnapshot(snapshot, vmId, new OVirtClient.SimpleResponse<Void>() {
            @Override
            public void onResponse(Void aVoid) throws RemoteException {
                snapshotFacade.syncAll(vmId);
            }
        });
    }

    @OptionsItem(R.id.action_spice_console)
    @Background
    void openSpiceConsole() {
        openConsole(Display.SPICE);
    }

    @OptionsItem(R.id.action_vnc_console)
    @Background
    void openVncConsole() {
        openConsole(Display.VNC);
    }

    private void openConsole(final Display display) {
        vmFacade.syncOne(new ProgressBarResponse<Vm>(this) {
            @Override
            public void onResponse(final Vm vm) throws RemoteException {
                ConsoleConnectionDetails details = null;

                if (movirtAuthenticator.isV3Api()) {
                    details = new ConsoleConnectionDetails(vm.getDisplayType(),
                            vm.getDisplayAddress(),
                            vm.getDisplayPort(),
                            vm.getDisplaySecurePort(),
                            vm.getCertificateSubject());
                } else { // V4 API
                    for (Console console : consoles) {
                        if (console.getDisplayType() == display) {
                            details = new ConsoleConnectionDetails(display,
                                    console.getAddress(),
                                    console.getPort(),
                                    console.getTlsPort(),
                                    vm.getCertificateSubject());
                            break;
                        }
                    }
                }
                connectToConsole(details);
            }
        }, vmId);
    }

    private void connectToConsole(final ConsoleConnectionDetails details) {
        client.getConsoleTicket(vmId, new ProgressBarResponse<ActionTicket>(this) {
            @Override
            public void onResponse(ActionTicket ticket) throws RemoteException {
                try {
                    if (details.getDisplay() == Display.SPICE && details.getTlsPort() > 0 && !isCaFileExists()) {
                        showMissingCaCertDialog();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW)
                                .setType("application/vnd.vnc")
                                .setData(Uri.parse(makeConsoleUrl(details, ticket)));
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

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showMissingCaCertDialog() {
        ImportCertificateDialogFragment importCertificateDialog = ImportCertificateDialogFragment
                .newSpiceCaInstance(getString(R.string.can_not_run_console_without_ca),
                        authenticator.getCertHandlingStrategy().id(),
                        authenticator.getApiUrl());
        importCertificateDialog.show(getFragmentManager(), "certificateDialog");
    }

    @Background
    void refreshConsoles() {
        if (movirtAuthenticator.isV4Api()) {
            consoleFacade.syncAll(new ProgressBarResponse<List<Console>>(this), vmId);
        }
    }

    private void syncVm() {
        vmFacade.syncOne(new ProgressBarResponse<Vm>(this), vmId);
        refreshConsoles();
    }

    /**
     * Returns URL for running console intent.
     *
     * @throws java.lang.IllegalArgumentException with description
     *                                            if the URL can't be created from input.
     */
    private String makeConsoleUrl(ConsoleConnectionDetails details, ActionTicket ticket)
            throws IllegalArgumentException {

        if (details.getDisplay() == null) {
            throw new IllegalArgumentException("Vm's display type cannot be null");
        }

        String parameters = "";
        if (ticket != null && ticket.ticket != null && ticket.ticket.value != null
                && !ticket.ticket.value.isEmpty()) {
            switch (details.getDisplay()) {
                case VNC:
                    parameters = Constants.PARAM_VNC_PWD + "=" + ticket.ticket.value; // vnc password
                    break;
                case SPICE:
                    parameters = Constants.PARAM_SPICE_PWD + "=" + ticket.ticket.value; // spice password
                    if (details.getTlsPort() > 0) {
                        String caCertPath = Constants.getCaCertPath(this);
                        String tlsPortPart = Constants.PARAM_TLS_PORT + "=" + details.getTlsPort();
                        String certSubjectPart = Constants.PARAM_CERT_SUBJECT + "=" + details.getCertificateSubject();
                        String caCertPathPart = Constants.PARAM_CA_CERT_PATH + "=" + caCertPath;

                        parameters += "&" + tlsPortPart + "&" + certSubjectPart + "&" + caCertPathPart;
                    }
                    break;
            }
        }

        return details.getDisplay().getProtocol() + "://" + details.getAddress() + ":" + details.getPort() + "?" + parameters;
    }


    private boolean isCaFileExists() {
        File file = new File(Constants.getCaCertPath(this));
        return file.exists();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    /**
     * Refreshes VM upon success
     */
    private class SyncVmResponse extends OVirtClient.SimpleResponse<Void> {
        @Override
        public void onResponse(Void obj) throws RemoteException {
            syncVm();
        }
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }

    private class ConsoleConnectionDetails {
        private Display display;
        private String address;
        private int port;
        private int tlsPort;
        private String certificateSubject;

        public ConsoleConnectionDetails(Display display, String address, int port, int tlsPort, String certificateSubject) {
            this.display = display;
            this.address = address;
            this.port = port;
            this.tlsPort = tlsPort;
            this.certificateSubject = certificateSubject;
        }

        public Display getDisplay() {
            return display;
        }

        public void setDisplay(Display display) {
            this.display = display;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getTlsPort() {
            return tlsPort;
        }

        public void setTlsPort(int tlsPort) {
            this.tlsPort = tlsPort;
        }

        public String getCertificateSubject() {
            return certificateSubject;
        }

        public void setCertificateSubject(String certificateSubject) {
            this.certificateSubject = certificateSubject;
        }
    }
}
