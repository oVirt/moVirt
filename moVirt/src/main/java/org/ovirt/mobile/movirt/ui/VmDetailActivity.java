package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
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
public class VmDetailActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String VM_URI = "vm_uri";
    private static final String TAG = VmDetailActivity.class.getSimpleName();
    public String vmId = null;

    @Bean
    OVirtClient client;

    @Bean
    ProviderFacade provider;

    @ViewById
    Button runButton;

    @ViewById
    Button stopButton;

    @ViewById
    Button rebootButton;

    @ViewById
    Button consoleButton;

    @ViewById
    Button eventsButton;

    @ViewById
    Button disksButton;

    @ViewById
    TextView statusView;

    @ViewById
    TextView cpuView;

    @ViewById
    TextView memView;

    @ViewById
    TextView memoryView;

    @ViewById
    TextView socketView;

    @ViewById
    TextView coreView;

    @ViewById
    TextView osView;

    @ViewById
    TextView displayView;

    @ViewById
    ProgressBar vncProgress;

    @StringRes(R.string.details_for_vm)
    String VM_DETAILS;

    Vm vm;

    Bundle args;

    @AfterViews
    void initLoader() {

        hideProgressBar();
        Uri vmUri = getIntent().getData();


        args = new Bundle();
        args.putParcelable(VM_URI, vmUri);
        getLoaderManager().initLoader(0, args, this);
        vmId = vmUri.getLastPathSegment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Click(R.id.runButton)
    @Background
    void start() {
        client.startVm(vm);
    }

    @Click(R.id.stopButton)
    @Background
    void stop() {
        client.stopVm(vm);
    }

    @Click(R.id.rebootButton)
    @Background
    void reboot() {
        client.rebootVm(vm);
    }

    @Click(R.id.consoleButton)
    @Background
    void openConsole() {
        showProgressBar();

        client.getVm(vm, new OVirtClient.SimpleResponse<ExtendedVm>() {
            @Override
            public void onResponse(final ExtendedVm freshVm) throws RemoteException {
                showProgressBar();

                client.getConsoleTicket(vm, new OVirtClient.SimpleResponse<ActionTicket>() {
                    @Override
                    public void onResponse(ActionTicket ticket) throws RemoteException {
                        hideProgressBar();
                        ExtendedVm.Display display = freshVm.display;
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW)
                                    .setType("application/vnd.vnc")
                                    .setData(Uri.parse(makeConsoleUrl(display, ticket)));
                            startActivity(intent);
                        } catch (IllegalArgumentException e) {
                            makeToast(e.getMessage());
                        } catch (Exception e) {
                            makeToast("Failed to open console client. Check if aSPICE/bVNC is installed.");
                        }

                    }
                });
            }

            @Override
            public void onError() {
                super.onError();

                hideProgressBar();
            }
        });
    }

    /**
     * Returns URL for running console intent.
     * @throws java.lang.IllegalArgumentException with description
     *   if the URL can't be created from input.
     */
    private String makeConsoleUrl(ExtendedVm.Display display, ActionTicket ticket)
            throws IllegalArgumentException
    {
        if (display == null) {
            throw new IllegalArgumentException("Illegal parameters for creating console intent URL.");
        }
        if (!"vnc".equals(display.type) && !"spice".equals(display.type)) {
            throw new IllegalArgumentException("Unknown console type: " + display.type);
        }

        String passwordPart = "";
        if (ticket != null && ticket.ticket != null && ticket.ticket.value != null
                && !ticket.ticket.value.isEmpty()) {
            switch (display.type) {
                case "vnc":
                    passwordPart = "VncPassword";
                    break;
                case "spice":
                    passwordPart = "SpicePassword";
                    break;
            }
            passwordPart += "=" + ticket.ticket.value;
        }

        return display.type + "://" + display.address + ":" + display.port + "?" + passwordPart;
    }

    @UiThread
    void showProgressBar() {
        consoleButton.setClickable(false);
        vncProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    void hideProgressBar() {
        consoleButton.setClickable(true);
        vncProgress.setVisibility(View.GONE);
    }

    @UiThread
    void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Click(R.id.eventsButton)
    void openEventsActivity() {
        final Intent intent = new Intent(this, EventsActivity_.class);
        intent.putExtra(EventsActivity.FILTER_VM_ID, vmId);
        startActivity(intent);
    }

    @Click(R.id.disksButton)
    void openDiskDetailActivity() {
        final Intent intent = new Intent(this, DiskDetailActivity_.class);
        intent.putExtra(DiskDetailActivity.FILTER_VM_ID, vmId);
        startActivity(intent);
    }

    @OptionsItem(R.id.action_edit_triggers)
    void editTriggers() {
        final Intent intent = new Intent(this, EditTriggersActivity_.class);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_ID, vm.getId());
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_NAME, vm.getName());
        intent.putExtra(EditTriggersActivity.EXTRA_SCOPE, Trigger.Scope.ITEM);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String vmId = args.<Uri>getParcelable(VM_URI).getLastPathSegment();
        return provider.query(Vm.class).id(vmId).asLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Vm");
            return;
        }
        vm = EntityMapper.VM_MAPPER.fromCursor(data);
        setTitle(String.format(VM_DETAILS, vm.getName()));
        statusView.setText(vm.getStatus().toString());
        cpuView.setText(String.format("%.2f%%", vm.getCpuUsage()));
        memView.setText(String.format("%.2f%%", vm.getMemoryUsage()));

        updateCommandButtons(vm);
        loadAdditionalVmData(vm);
    }

    private void updateCommandButtons(Vm vm) {
        runButton.setClickable(Vm.Command.RUN.canExecute(vm.getStatus()));
        stopButton.setClickable(Vm.Command.POWEROFF.canExecute(vm.getStatus()));
        rebootButton.setClickable(Vm.Command.REBOOT.canExecute(vm.getStatus()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @UiThread
    public void renderVm(ExtendedVm vm, VmStatistics statistics) {
        Long memoryMB = 0L;
        boolean memoryExceptionFlag = false;
        setTitle(String.format(VM_DETAILS, vm.name));
        statusView.setText(vm.status.state);
        cpuView.setText(String.format("%.2f%%", statistics.getCpuUsage()));
        memView.setText(String.format("%.2f%%", statistics.getMemoryUsage()));
        try {
            memoryMB = Long.parseLong(vm.memory);
        }
        catch (Exception e) {
            memoryExceptionFlag = true;
        }
        if(!memoryExceptionFlag) {
            memoryMB = memoryMB / (1024 * 1024);
            memoryView.setText(memoryMB + " MB");
        }
        else {
            memoryView.setText("N/A");
        }
        socketView.setText(vm.cpu.topology.sockets);
        coreView.setText(vm.cpu.topology.cores);
        osView.setText(vm.os.type);
        if (vm.display != null && vm.display.type != null) {
            displayView.setText(vm.display.type);
        }
        else {
            displayView.setText("N/A");
        }

        updateCommandButtons(vm);
    }

    private void updateCommandButtons(org.ovirt.mobile.movirt.rest.Vm vm) {
        Vm.Status status = Vm.Status.valueOf(vm.status.state.toUpperCase());
        runButton.setClickable(Vm.Command.RUN.canExecute(status));
        stopButton.setClickable(Vm.Command.POWEROFF.canExecute(status));
        rebootButton.setClickable(Vm.Command.REBOOT.canExecute(status));
    }

    @Receiver(actions = Broadcasts.CONNECTION_FAILURE, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void connectionFailure(@Receiver.Extra(Broadcasts.Extras.CONNECTION_FAILURE_REASON) String reason) {
        Toast.makeText(VmDetailActivity.this, R.string.rest_req_failed + " " + reason, Toast.LENGTH_LONG).show();
    }

    @Background
    void loadAdditionalVmData(final Vm vm) {
        showProgressBar();

        client.getVm(vm, new OVirtClient.SimpleResponse<ExtendedVm>() {
            @Override
            public void onResponse(final ExtendedVm loadedVm) throws RemoteException {
                client.getVmStatistics(vm, new OVirtClient.SimpleResponse<VmStatistics>() {
                    @Override
                    public void onResponse(VmStatistics vmStatistics) throws RemoteException {
                        hideProgressBar();

                        renderVm(loadedVm, vmStatistics);
                    }
                });
            }

            @Override
            public void onError() {
                super.onError();

                hideProgressBar();
            }
        });

    }
}
