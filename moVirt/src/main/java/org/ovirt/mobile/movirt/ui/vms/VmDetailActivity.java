package org.ovirt.mobile.movirt.ui.vms;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.model.enums.ConsoleProtocol;
import org.ovirt.mobile.movirt.model.enums.VmCommand;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.PresenterStatusSyncableActivity;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.CreateSnapshotDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.CreateSnapshotDialogFragment_;
import org.ovirt.mobile.movirt.ui.dialogs.DialogListener;
import org.ovirt.mobile.movirt.ui.events.VmEventsFragment;
import org.ovirt.mobile.movirt.ui.events.VmEventsFragment_;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity;
import org.ovirt.mobile.movirt.ui.triggers.EditTriggersActivity_;

@EActivity(R.layout.activity_vm_detail)
@OptionsMenu(R.menu.vm)
public class VmDetailActivity extends PresenterStatusSyncableActivity implements HasProgressBar,
        ConfirmDialogFragment.ConfirmDialogListener, DialogListener.NewSnapshotListener,
        VmDetailContract.View {
    private static final int REQUEST_MIGRATE = 0;
    private static final int ACTION_STOP_VM = 0;
    private static final int ACTION_REBOOT_VM = 1;
    private static final int ACTION_STOP_MIGRATE_VM = 2;

    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringArrayRes(R.array.vm_detail_pager_titles)
    String[] PAGER_TITLES;
    @ViewById
    ProgressBar progress;
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

    private MenuState menuState = MenuState.EMPTY;

    private MovirtAccount account;
    private VmDetailContract.Presenter presenter;

    @AfterViews
    void init() {
        Uri vmUri = getIntent().getData();
        vmId = vmUri.getLastPathSegment();
        account = getIntent().getParcelableExtra(org.ovirt.mobile.movirt.Constants.ACCOUNT_KEY);
        presenter = VmDetailPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .setVmId(vmId)
                .setAccount(account)
                .initialize();

        initPagers();
        setProgressBar(progress);
    }

    @Override
    public BasePresenter getPresenter() {
        return presenter;
    }

    private void initPagers() {

        VmEventsFragment eventList = new VmEventsFragment_();
        VmDisksFragment diskList = new VmDisksFragment_();
        VmNicsFragment nicList = new VmNicsFragment_();
        VmSnapshotsFragment snapshotList = new VmSnapshotsFragment_();

        eventList.setVmId(vmId)
                .setAccount(account);

        diskList.setVmId(vmId);
        diskList.setAccount(account);

        nicList.setVmId(vmId);
        nicList.setAccount(account);

        snapshotList.setVmId(vmId);
        snapshotList.setAccount(account);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new VmDetailGeneralFragment_(),
                eventList,
                snapshotList,
                diskList,
                nicList);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    @Override
    public void displayMenu(MenuState menuState) {
        this.menuState = menuState;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuRun.setVisible(menuState.hasStatus() && VmCommand.RUN.canExecute(menuState.status));
        menuStop.setVisible(menuState.hasStatus() && VmCommand.STOP.canExecute(menuState.status));
        menuReboot.setVisible(menuState.hasStatus() && VmCommand.REBOOT.canExecute(menuState.status));
        menuStartMigration.setVisible(menuState.hasStatus() && VmCommand.START_MIGRATION.canExecute(menuState.status));
        menuCancelMigration.setVisible(menuState.hasStatus() && VmCommand.CANCEL_MIGRATION.canExecute(menuState.status));

        menuCreateSnapshot.setVisible(menuState.createSnapshotVisibility);

        boolean consoleExecutable = menuState.hasStatus() && VmCommand.CONSOLE.canExecute(menuState.status);
        menuSpiceConsole.setVisible(consoleExecutable && menuState.hasSpiceConsole);
        menuVncConsole.setVisible(consoleExecutable && menuState.hasVncConsole);

        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_edit_triggers)
    void editTriggers() {
        presenter.editTriggers();
    }

    @OptionsItem(R.id.action_run)
    void start() {
        presenter.startVm();
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
                    presenter.stopVm();
                    break;
                case ACTION_REBOOT_VM:
                    presenter.rebootVm();
                    break;
                case ACTION_STOP_MIGRATE_VM:
                    presenter.cancelMigration();
                    break;
            }
        }
    }

    @OptionsItem(R.id.action_start_migration)
    void showMigrationDialog() {
        presenter.beginMigration();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MIGRATE) {
            if (resultCode == VmMigrateActivity.RESULT_DEFAULT) {
                presenter.migrateToDefault();
            }
            if (resultCode == VmMigrateActivity.RESULT_SELECT) {
                presenter.migrateTo(data.getStringExtra(VmMigrateActivity.RESULT_EXTRA_HOST_ID));
            }
        }
    }

    @OptionsItem(R.id.action_create_snapshot)
    void createSnapshot() {
        CreateSnapshotDialogFragment dialog = new CreateSnapshotDialogFragment_();
        dialog.setVmId(vmId);
        dialog.setAccount(account);
        dialog.show(getFragmentManager(), "createSnapshot");
    }

    @Override
    public void onDialogResult(org.ovirt.mobile.movirt.rest.dto.Snapshot snapshot) {
        presenter.createSnapshot(snapshot);
    }

    @OptionsItem(R.id.action_spice_console)
    void openSpiceConsole() {
        presenter.openConsole(ConsoleProtocol.SPICE);
    }

    @OptionsItem(R.id.action_vnc_console)
    void openVncConsole() {
        presenter.openConsole(ConsoleProtocol.VNC);
    }

    @Override
    public void startMigrationActivity(Selection selection, String hostId, String clusterId) {
        Intent migrateIntent = new Intent(this, VmMigrateActivity_.class);
        migrateIntent.putExtra(VmMigrateActivity.EXTRA_SELECTION, selection);
        migrateIntent.putExtra(VmMigrateActivity.EXTRA_HOST_ID, hostId);
        migrateIntent.putExtra(VmMigrateActivity.EXTRA_CLUSTER_ID, clusterId);
        startActivityForResult(migrateIntent, REQUEST_MIGRATE);
    }

    @Override
    public void startConsoleActivity(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setType("application/vnd.vnc")
                .setData(uri);
        startActivity(intent);
    }

    @Override
    public void startEditTriggersActivity(Selection selection, String vmId) {
        final Intent intent = new Intent(this, EditTriggersActivity_.class);
        intent.putExtra(EditTriggersActivity.EXTRA_SELECTION, selection);
        intent.putExtra(EditTriggersActivity.EXTRA_TARGET_ENTITY_ID, vmId);
        startActivity(intent);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }
}
