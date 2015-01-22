package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
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
    Button vncButton;

    @ViewById
    TextView statusView;

    @ViewById
    TextView cpuView;

    @ViewById
    TextView memView;

    @ViewById
    ProgressBar vncProgress;

    @FragmentById
    EventsFragment eventList;

    @StringRes(R.string.details_for_vm)
    String VM_DETAILS;

    Vm vm;
    Bundle args;

    @Bean
    OVirtClient oVirtClient;

    @AfterViews
    void initLoader() {

        hideProgressBar();
        Uri vmUri = getIntent().getData();
        args = new Bundle();
        args.putParcelable(VM_URI, vmUri);
        getLoaderManager().initLoader(0, args, this);
        eventList.setFilterVmId(vmUri.getLastPathSegment());
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

    @Click(R.id.vncButton)
    @Background
    void openVncConsole() {
        String address, port, type;
        Intent intent;
        showProgressBar();
        ExtendedVm freshVm = client.getVm(vm);
        ActionTicket ticket = client.getConsoleTicket(vm);
        address = freshVm.display.address;
        port = freshVm.display.port;
        type = freshVm.display.type;
        hideProgressBar();
        if ("vnc".equals(type)) {
            try {
                intent = new Intent(Intent.ACTION_VIEW).setType("application/vnd.vnc").setData(Uri.parse(type + "://" + address + ":" + port + "?VncPassword=" + ticket.ticket.value));
                startActivity(intent);
            }
            catch (Exception e) {
                String msg = "moVirt failed to open bVnc. Check if bVnc is installed.";
                makeToast(msg);
            }
        }
        else {
            String msg = "The console is not a VNC console. Check the type of console.";
            makeToast(msg);
        }
    };

    @UiThread
    void showProgressBar() {
        vncButton.setClickable(false);
        vncProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    void hideProgressBar() {
        vncButton.setClickable(true);
        vncProgress.setVisibility(View.GONE);
    }

    @UiThread
    void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
    public void renderVm(org.ovirt.mobile.movirt.rest.Vm vm, VmStatistics statistics) {
        setTitle(String.format(VM_DETAILS, vm.name));
        statusView.setText(vm.status.state);
        cpuView.setText(String.format("%.2f%%", statistics.getCpuUsage()));
        memView.setText(String.format("%.2f%%", statistics.getMemoryUsage()));

        updateCommandButtons(vm);
    }

    private void updateCommandButtons(org.ovirt.mobile.movirt.rest.Vm vm) {
        Vm.Status status = Vm.Status.valueOf(vm.status.state.toUpperCase());
        runButton.setClickable(Vm.Command.RUN.canExecute(status));
        stopButton.setClickable(Vm.Command.POWEROFF.canExecute(status));
        rebootButton.setClickable(Vm.Command.REBOOT.canExecute(status));
    }

    @Background
    void loadAdditionalVmData(Vm vm) {
        showProgressBar();
        ExtendedVm loadedVm = oVirtClient.getVm(vm);
        VmStatistics statistics = oVirtClient.getVmStatistics(vm);
        hideProgressBar();

        renderVm(loadedVm, statistics);
    }
}
