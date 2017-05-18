package org.ovirt.mobile.movirt.ui.snapshots;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.enums.SnapshotStatus;
import org.ovirt.mobile.movirt.model.enums.VmCommand;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.ui.BooleanListener;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.PresenterStatusSyncableActivity;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.PreviewRestoreSnapshotDialogFragment;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;

@EActivity(R.layout.activity_snapshot_detail)
@OptionsMenu(R.menu.snapshot)
public class SnapshotDetailActivity extends PresenterStatusSyncableActivity implements SnapshotDetailContract.View,
        ConfirmDialogFragment.ConfirmDialogListener, BooleanListener {

    private static final int DELETE_ACTION = 0;
    private static final int PREVIEW_ACTION = 1;
    private static final int RESTORE_ACTION = 2;

    @ViewById
    ViewPager viewPager;

    @ViewById
    PagerTabStrip pagerTabStrip;

    @StringArrayRes(R.array.snapshot_detail_pager_titles)
    String[] PAGER_TITLES;

    @ViewById
    ProgressBar progress;

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

    private MenuState menuState = MenuState.EMPTY;

    private SnapshotDetailContract.Presenter presenter;

    @AfterViews
    public void init() {
        Intent intent = getIntent();
        Uri snapshotUri = intent.getData();
        String snapshotId = snapshotUri.getLastPathSegment();
        String vmId = intent.getExtras().getString(OVirtContract.HasVm.VM_ID);
        MovirtAccount account = intent.getParcelableExtra(org.ovirt.mobile.movirt.Constants.ACCOUNT_KEY);

        presenter = SnapshotDetailPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .setIds(snapshotId, vmId)
                .setAccount(account)
                .initialize();

        initPagers(vmId, snapshotId, account);
        setProgressBar(progress);
    }

    @Override
    protected BasePresenter getPresenter() {
        return presenter;
    }

    private void initPagers(String vmId, String snapshotId, MovirtAccount account) {
        SnapshotVmDetailGeneralFragment snapshotVmDetailFragment = new SnapshotVmDetailGeneralFragment_();
        SnapshotDisksFragment diskList = new SnapshotDisksFragment_();
        SnapshotNicsFragment nicList = new SnapshotNicsFragment_();

        snapshotVmDetailFragment.setVmId(vmId);
        snapshotVmDetailFragment.setSnapshotId(snapshotId);
        snapshotVmDetailFragment.setMovirtAccount(account);

        diskList.setVmId(vmId);
        diskList.setSnapshotId(snapshotId);
        diskList.setAccount(account);

        nicList.setVmId(vmId);
        nicList.setSnapshotId(snapshotId);
        nicList.setAccount(account);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                snapshotVmDetailFragment,
                diskList,
                nicList
        );

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    @Override
    public void displayMenu(MenuState menuState) {
        this.menuState = menuState;
        invalidateOptionsMenu();
    }

    //
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean vmOk = menuState.hasBothStatuses() && VmCommand.NOT_RUNNING.canExecute(menuState.vmStatus);
        boolean commitUndoVisible = vmOk && SnapshotStatus.IN_PREVIEW.equals(menuState.snapshotStatus);

        menuPreview.setVisible(vmOk && menuState.allSnapshotsOK);
        menuRestore.setVisible(vmOk && menuState.allSnapshotsOK);
        menuCommit.setVisible(vmOk && commitUndoVisible);
        menuUndo.setVisible(vmOk && commitUndoVisible);

        menuDelete.setVisible(menuState.hasSnapshotStatus() && menuState.allSnapshotsOK);
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_delete)
    void delete() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(DELETE_ACTION, getString(R.string.dialog_action_delete_snapshot));
        confirmDialog.show(getFragmentManager(), "confirmDeleteSnapshot");
    }

    @Override
    public void openPreviewDialog() {
        PreviewRestoreSnapshotDialogFragment dialog = PreviewRestoreSnapshotDialogFragment
                .newInstance(PREVIEW_ACTION, getString(R.string.preview));
        dialog.show(getFragmentManager(), "previewSnapshot");
    }

    @Override
    public void openRestoreDialog() {
        PreviewRestoreSnapshotDialogFragment dialog = PreviewRestoreSnapshotDialogFragment
                .newInstance(RESTORE_ACTION, getString(R.string.restore));
        dialog.show(getFragmentManager(), "restoreSnapshot");
    }

    @OptionsItem(R.id.action_preview)
    public void preview() {
        presenter.onPreviewSnapshot();
    }

    @OptionsItem(R.id.action_restore)
    public void restore() {
        presenter.onRestoreSnapshot();
    }

    @OptionsItem(R.id.action_commit)
    public void commit() {
        presenter.commitSnapshot();
    }

    @OptionsItem(R.id.action_undo)
    public void undo() {
        presenter.undoSnapshot();
    }

    //
    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (actionId == DELETE_ACTION && dialogButton == DialogInterface.BUTTON_POSITIVE) {
            presenter.deleteSnapshot();
        }
    }

    @Override
    public void onDialogResult(int actionId, boolean restoreMemory) {
        switch (actionId) {
            case PREVIEW_ACTION:
                presenter.previewSnapshot(restoreMemory);
                break;
            case RESTORE_ACTION:
                presenter.restoreSnapshot(restoreMemory);
                break;
        }
    }
}
