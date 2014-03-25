package org.ovirt.mobile.movirt.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;
import org.ovirt.mobile.movirt.util.CursorAdapterLoader;


@EActivity(R.layout.activity_vm_detail)
public class VmDetailActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String VM_URI = "vm_uri";
    private static final String[] PROJECTION = OVirtContract.Vm.ALL_COLUMNS;
    private static final String TAG = VmDetailActivity.class.getSimpleName();

    @Bean
    OVirtClient client;

    @ViewById
    TextView titleView;

    @ViewById
    Button runButton;

    @ViewById
    Button stopButton;

    @ViewById
    Button rebootButton;

    @ViewById
    TextView statusView;

    @ViewById
    TextView cpuView;

    @ViewById
    TextView memView;

    @ViewById
    ListView eventListView;

    Vm vm;
    Bundle args;

    private SimpleCursorAdapter eventListAdapter;

    private CursorAdapterLoader eventsLoader;

    @AfterViews
    void initLoader() {
        initEventListAdapter();

        Uri vmUri = getIntent().getData();
        args = new Bundle();
        args.putParcelable(VM_URI, vmUri);
        getLoaderManager().initLoader(0, args, this);
        getLoaderManager().initLoader(1, args, eventsLoader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, args, this);
        getLoaderManager().restartLoader(1, args, eventsLoader);
    }

    private void initEventListAdapter() {
        eventListAdapter = new SimpleCursorAdapter(this,
                                                   R.layout.event_list_item,
                                                   null,
                                                   new String[] {OVirtContract.Event.TIME, OVirtContract.Event.DESCRIPTION},
                                                   new int[] {R.id.event_timestamp, R.id.event_description});

        eventsLoader = new CursorAdapterLoader(eventListAdapter) {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri vmUri = args.getParcelable(VM_URI);
                String vmId = vmUri.getLastPathSegment();
                return new CursorLoader(VmDetailActivity.this,
                                        OVirtContract.Event.CONTENT_URI,
                                        null,
                                        OVirtContract.Event.VM_ID + " = ?",
                                        new String[] {vmId},
                                        OVirtContract.Event.ID + " desc");
            }
        };

        eventListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                return false;
            }
        });
        eventListView.setAdapter(eventListAdapter);
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

    @Click(R.id.triggerButton)
    void editTriggers() {
        final Intent intent = new Intent(this, EditTriggersActivity_.class);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_ID, vm.getId());
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_NAME, vm.getName());
        intent.putExtra(EditTriggersActivity.EXTRA_SCOPE, Trigger.Scope.ITEM);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, args.<Uri>getParcelable(VM_URI), PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Vm");
            return;
        }
        vm = EntityMapper.VM_MAPPER.fromCursor(data);
        titleView.setText(vm.getName());
        statusView.setText(vm.getStatus().toString());
        cpuView.setText(String.format("%.2f%%", vm.getCpuUsage()));
        memView.setText(String.format("%.2f%%", vm.getMemoryUsage()));

        updateCommandButtons(vm);
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
}
