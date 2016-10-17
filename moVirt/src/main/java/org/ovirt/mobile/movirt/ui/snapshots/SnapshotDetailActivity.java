package org.ovirt.mobile.movirt.ui.snapshots;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.SnapshotFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.rest.dto.SnapshotAction;
import org.ovirt.mobile.movirt.ui.BooleanListener;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.MovirtActivity;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.PreviewRestoreSnapshotDialogFragment;
import org.ovirt.mobile.movirt.ui.vms.VmDetailGeneralFragment;
import org.ovirt.mobile.movirt.ui.vms.VmDetailGeneralFragment_;
import org.ovirt.mobile.movirt.ui.vms.VmDisksFragment;
import org.ovirt.mobile.movirt.ui.vms.VmDisksFragment_;
import org.ovirt.mobile.movirt.ui.vms.VmNicsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmNicsFragment_;

import java.util.Collection;

import static org.springframework.util.StringUtils.isEmpty;

@EActivity(R.layout.activity_snapshot_detail)
@OptionsMenu(R.menu.snapshot)
public class SnapshotDetailActivity extends MovirtActivity implements HasProgressBar,
        LoaderManager.LoaderCallbacks<Cursor>,
        ConfirmDialogFragment.ConfirmDialogListener, BooleanListener {

    private static final String TAG = SnapshotDetailActivity.class.getSimpleName();

    private static final int DELETE_ACTION = 0;
    private static final int PREVIEW_ACTION = 1;
    private static final int RESTORE_ACTION = 2;

    private static final int SNAPSHOT_LOADER = 1; // 0 in MovirtActivity
    private static final int SNAPSHOTS_LOADER = 2;
    private static final int VMS_LOADER = 3;

    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringArrayRes(R.array.snapshot_detail_pager_titles)
    String[] PAGER_TITLES;
    @ViewById
    ProgressBar progress;

    @StringRes(R.string.details_for_snapshot)
    String SNAPSHOT_DETAILS;

    @Bean
    ProviderFacade provider;

    @Bean
    VmFacade vmFacade;

    @Bean
    SnapshotFacade snapshotFacade;

    @Bean
    OVirtClient client;

    @OptionsMenuItem(R.id.action_delete)
    MenuItem menuDelete;

    @OptionsMenuItem(R.id.action_preview)
    MenuItem menuPreview;

    @OptionsMenuItem(R.id.action_restore)
    MenuItem menuRestore;

    @OptionsMenuItem(R.id.action_commit)
    MenuItem menuCommit;

    @OptionsMenuItem(R.id.action_undo)
    MenuItem menuUndo;

    @InstanceState
    protected String snapshotId;

    @InstanceState
    protected String vmId;

    private Snapshot currentSnapshot;
    private Vm vm;
    private Collection<Snapshot> snapshots;

    @AfterViews
    public void init() {
        if (isEmpty(snapshotId) && isEmpty(vmId)) {
            Intent intent = getIntent();
            Uri snapshotUri = intent.getData();
            snapshotId = snapshotUri.getLastPathSegment();
            vmId = intent.getExtras().getString(OVirtContract.HasVm.VM_ID);

            // for vm fragment
            Uri vmUri = OVirtContract.Vm.CONTENT_URI.buildUpon().appendPath(vmId + snapshotId).build();
            intent.setData(vmUri);
        }


        initLoaders();
        initPagers();
        setProgressBar(progress);
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(SNAPSHOT_LOADER, null, this);
        getSupportLoaderManager().initLoader(SNAPSHOTS_LOADER, null, this);
        getSupportLoaderManager().initLoader(VMS_LOADER, null, this);
    }

    @Override
    public void restartLoader() {
        super.restartLoader();
        getSupportLoaderManager().restartLoader(SNAPSHOT_LOADER, null, this);
        getSupportLoaderManager().restartLoader(SNAPSHOTS_LOADER, null, this);
        getSupportLoaderManager().restartLoader(VMS_LOADER, null, this);
    }

    @Override
    public void destroyLoader() {
        super.destroyLoader();
        getSupportLoaderManager().destroyLoader(SNAPSHOT_LOADER);
        getSupportLoaderManager().destroyLoader(SNAPSHOTS_LOADER);
        getSupportLoaderManager().destroyLoader(VMS_LOADER);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;

        switch (id) {
            case SNAPSHOT_LOADER:
                loader = provider.query(Snapshot.class).id(snapshotId).asLoader();
                break;
            case SNAPSHOTS_LOADER:
                loader = provider.query(Snapshot.class).where(OVirtContract.Snapshot.VM_ID, vmId).asLoader();
                break;
            case VMS_LOADER:
                loader = provider.query(Vm.class).id(vmId).asLoader();
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Snapshot");
            return;
        }

        switch (loader.getId()) {
            case SNAPSHOT_LOADER:
                currentSnapshot = snapshotFacade.mapFromCursor(data);
                setTitle(String.format(SNAPSHOT_DETAILS, currentSnapshot.getName()));
                break;
            case SNAPSHOTS_LOADER:
                snapshots = snapshotFacade.mapAllFromCursor(data);
                break;
            case VMS_LOADER:
                vm = vmFacade.mapFromCursor(data);
                break;
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    private void initPagers() {

        VmDetailGeneralFragment vmDetailFragment = new VmDetailGeneralFragment_();
        VmDisksFragment diskList = new VmDisksFragment_();
        VmNicsFragment nicList = new VmNicsFragment_();

        diskList.setFilterVmId(vmId);
        diskList.setFilterSnapshotId(snapshotId);
        nicList.setFilterVmId(vmId);
        nicList.setFilterSnapshotId(snapshotId);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                vmDetailFragment,
                diskList,
                nicList
        );

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentSnapshot != null && snapshots != null) {
            boolean allOk = !Snapshot.containsOneOfStatuses(snapshots, Snapshot.SnapshotStatus.LOCKED, Snapshot.SnapshotStatus.IN_PREVIEW);

            if (vm != null && Vm.Command.NOT_RUNNING.canExecute(vm.getStatus())) {
                boolean commitUndoVisible = Snapshot.SnapshotStatus.IN_PREVIEW.equals(currentSnapshot.getSnapshotStatus());

                menuPreview.setVisible(allOk);
                menuRestore.setVisible(allOk);
                menuCommit.setVisible(commitUndoVisible);
                menuUndo.setVisible(commitUndoVisible);
            }

            menuDelete.setVisible(allOk);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_delete)
    void delete() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(DELETE_ACTION, getString(R.string.dialog_action_delete_snapshot));
        confirmDialog.show(getFragmentManager(), "confirmDeleteSnapshot");
    }

    @OptionsItem(R.id.action_preview)
    public void preview() {
        if (currentSnapshot.getPersistMemorystate()) {
            PreviewRestoreSnapshotDialogFragment dialog = PreviewRestoreSnapshotDialogFragment
                    .newInstance(PREVIEW_ACTION, getString(R.string.preview));
            dialog.show(getFragmentManager(), "previewSnapshot");
        } else {
            doPreviewSnapshot(false);
        }
    }


    @OptionsItem(R.id.action_restore)
    public void restore() {
        if (currentSnapshot.getPersistMemorystate()) {
            PreviewRestoreSnapshotDialogFragment dialog = PreviewRestoreSnapshotDialogFragment
                    .newInstance(RESTORE_ACTION, getString(R.string.restore));
            dialog.show(getFragmentManager(), "restoreSnapshot");
        } else {
            doRestoreSnapshot(false);
        }
    }

    @OptionsItem(R.id.action_commit)
    @Background
    public void doCommit() {
        client.commitSnapshot(vmId, getSyncSnapshotsResponse());
    }

    @OptionsItem(R.id.action_undo)
    @Background
    public void doUndo() {
        client.undoSnapshot(vmId, getSyncSnapshotsResponse());
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (actionId == DELETE_ACTION && dialogButton == DialogInterface.BUTTON_POSITIVE) {
            doDelete();
        }
    }

    @Override
    public void onDialogResult(int actionId, boolean restoreMemory) {
        switch (actionId) {
            case PREVIEW_ACTION:
                doPreviewSnapshot(restoreMemory);
                break;
            case RESTORE_ACTION:
                doRestoreSnapshot(restoreMemory);
                break;
        }
    }

    @Background
    public void doDelete() {
        client.deleteSnapshot(vmId, snapshotId, new SimpleResponse<Void>() {
            @Override
            public void onResponse(Void aVoid) throws RemoteException {
                snapshotFacade.syncAll(vmId);
                if (vm != null) {
                    startActivity(vmFacade.getDetailIntent(vm, getApplicationContext()));
                }
            }
        });
    }

    @Background
    public void doPreviewSnapshot(boolean restoreMemory) {
        SnapshotAction action = new SnapshotAction(snapshotId, restoreMemory);
        client.previewSnapshot(action, vmId, getSyncSnapshotsResponse());
    }

    @Background
    public void doRestoreSnapshot(boolean restoreMemory) {
        SnapshotAction action = new SnapshotAction(snapshotId, restoreMemory);
        client.restoreSnapshot(action, vmId, getSyncSnapshotsResponse());
    }

    @Background
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void syncing(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (syncing) {
            syncSnapshots();
        }
    }

    @NonNull
    private SimpleResponse<Void> getSyncSnapshotsResponse() {
        return new SimpleResponse<Void>() {
            @Override
            public void onResponse(Void aVoid) throws RemoteException {
                syncSnapshots();
            }
        };
    }

    private void syncSnapshots() {
        snapshotFacade.syncAll(vmId);
    }
}
