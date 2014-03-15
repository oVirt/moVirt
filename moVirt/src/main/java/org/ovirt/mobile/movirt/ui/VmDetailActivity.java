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
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Trigger;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;


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

    Vm vm;

    @AfterViews
    void initLoader() {
        Uri vmUri = getIntent().getData();
        Bundle args = new Bundle();
        args.putParcelable(VM_URI, vmUri);
        getLoaderManager().initLoader(0, args, this);
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

        updateCommandButtons(vm);
    }

    private void updateCommandButtons(Vm vm) {
        // TODO:
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
