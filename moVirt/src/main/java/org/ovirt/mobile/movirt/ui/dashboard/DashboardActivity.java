package org.ovirt.mobile.movirt.ui.dashboard;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.MovirtActivity;

@EActivity(R.layout.activity_dashboard)
@OptionsMenu(R.menu.dashboard)
public class DashboardActivity extends MovirtActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();

    @ViewById
    ProgressBar progress;

    @App
    MoVirtApp app;

    @OptionsMenuItem(R.id.action_switch_consumption_view)
    MenuItem menuSwitchConsumptionView;

    @InstanceState
    boolean virtualView = false;

    @AfterViews
    void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setProgressBar(progress);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }

    @OptionsItem(R.id.action_switch_consumption_view)
    @UiThread
    void switchConsumptionView() {
        DashboardGeneralFragment dashboardFragment = (DashboardGeneralFragment) getSupportFragmentManager().findFragmentById(R.id.dashboard_general_fragment);
        virtualView = !virtualView;
        invalidateOptionsMenu();
        dashboardFragment.setVirtualView(virtualView);
        getSupportFragmentManager().beginTransaction()
                .detach(dashboardFragment)
                .attach(dashboardFragment)
                .commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuSwitchConsumptionView.setTitle(virtualView ? R.string.show_physical : R.string.show_virtual);
        return super.onPrepareOptionsMenu(menu);
    }
}
