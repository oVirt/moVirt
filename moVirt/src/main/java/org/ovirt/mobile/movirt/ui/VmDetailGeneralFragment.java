package org.ovirt.mobile.movirt.ui;

import android.app.Fragment;
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
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.VmStatistics;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.ActionTicket;
import org.ovirt.mobile.movirt.rest.ExtendedVm;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

@EFragment(R.layout.fragment_vm_detail_general)
public class VmDetailGeneralFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = VmDetailGeneralFragment.class.getSimpleName();

    private static final String VM_URI = "vm_uri";

    private String vmId = null;

    @ViewById
    Button runButton;

    @ViewById
    Button stopButton;

    @ViewById
    Button rebootButton;

    @ViewById
    Button consoleButton;

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

    Bundle args;

    @Bean
    OVirtClient client;

    @Bean
    ProviderFacade provider;

    @StringRes(R.string.details_for_vm)
    String VM_DETAILS;

    Vm vm;

    @AfterViews
    void initLoader() {

        hideProgressBar();
        Uri vmUri = getActivity().getIntent().getData();


        args = new Bundle();
        args.putParcelable(VM_URI, vmUri);
        getLoaderManager().initLoader(0, args, this);
        vmId = vmUri.getLastPathSegment();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, args, this);
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
        getActivity().setTitle(String.format(VM_DETAILS, vm.getName()));
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
        getActivity().setTitle(String.format(VM_DETAILS, vm.name));
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

    @Override
    public void onPause() {
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
    void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
