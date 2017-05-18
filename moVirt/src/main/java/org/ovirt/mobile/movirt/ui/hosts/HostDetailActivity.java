package org.ovirt.mobile.movirt.ui.hosts;

import android.content.DialogInterface;
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
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.enums.HostCommand;
import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.ui.FragmentListPagerAdapter;
import org.ovirt.mobile.movirt.ui.HasProgressBar;
import org.ovirt.mobile.movirt.ui.PresenterStatusSyncableActivity;
import org.ovirt.mobile.movirt.ui.dialogs.ConfirmDialogFragment;
import org.ovirt.mobile.movirt.ui.events.HostEventsFragment;
import org.ovirt.mobile.movirt.ui.events.HostEventsFragment_;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment;
import org.ovirt.mobile.movirt.ui.vms.VmsFragment_;

@EActivity(R.layout.activity_host_detail)
@OptionsMenu(R.menu.host)
public class HostDetailActivity extends PresenterStatusSyncableActivity
        implements HasProgressBar, HostDetailContract.View,
        ConfirmDialogFragment.ConfirmDialogListener {

    @ViewById
    ViewPager viewPager;
    @ViewById
    PagerTabStrip pagerTabStrip;
    @StringArrayRes(R.array.host_detail_pager_titles)
    String[] PAGER_TITLES;
    @ViewById
    ProgressBar progress;
    @App
    MoVirtApp app;
    @OptionsMenuItem(R.id.action_activate)
    MenuItem menuActivate;
    @OptionsMenuItem(R.id.action_deactivate)
    MenuItem menuDeactivate;

    private String hostId = null;
    private HostStatus status;
    private MovirtAccount account;

    private HostDetailContract.Presenter presenter;

    @AfterViews
    void init() {
        hostId = getIntent().getData().getLastPathSegment();
        account = getIntent().getParcelableExtra(Constants.ACCOUNT_KEY);
        presenter = HostDetailPresenter_.getInstance_(getApplicationContext())
                .setView(this)
                .setHostId(hostId)
                .setAccount(account)
                .initialize();

        initPagers();
        setProgressBar(progress);
    }

    public HostDetailContract.Presenter getPresenter() {
        return presenter;
    }

    private void initPagers() {
        VmsFragment vmsFragment = new VmsFragment_();
        HostEventsFragment eventsFragment = new HostEventsFragment_();

        vmsFragment.setHostId(hostId);
        vmsFragment.setAccount(account);
        eventsFragment.setHostId(hostId)
                .setAccount(account);

        FragmentListPagerAdapter pagerAdapter = new FragmentListPagerAdapter(
                getSupportFragmentManager(), PAGER_TITLES,
                new HostDetailGeneralFragment_(),
                vmsFragment,
                eventsFragment);

        viewPager.setAdapter(pagerAdapter);
        pagerTabStrip.setTabIndicatorColorResource(R.color.material_deep_teal_200);
    }

    @Override
    public void displayHostStatus(HostStatus status) {
        this.status = status;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuActivate.setVisible(status != null && HostCommand.ACTIVATE.canExecute(status));
        menuDeactivate.setVisible(status != null && HostCommand.DEACTIVATE.canExecute(status));

        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_activate)
    void activate() {
        presenter.activate();
    }

    @OptionsItem(R.id.action_deactivate)
    void deactivate() {
        ConfirmDialogFragment confirmDialog = ConfirmDialogFragment
                .newInstance(0, getString(R.string.dialog_action_deactivate_host));
        confirmDialog.show(getFragmentManager(), "confirmDeactivateHost");
    }

    @Override
    public void onDialogResult(int dialogButton, int actionId) {
        if (dialogButton == DialogInterface.BUTTON_POSITIVE) {
            presenter.deactivate();
        }
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }
}
