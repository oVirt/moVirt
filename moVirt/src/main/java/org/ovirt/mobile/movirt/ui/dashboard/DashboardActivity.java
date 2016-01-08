package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Intent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.ui.MovirtActivity;

@EActivity(R.layout.activity_dashboard)
public class DashboardActivity extends MovirtActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();

    @ViewById
    ProgressBar progress;

    @App
    MoVirtApp app;

    @AfterViews
    void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setProgressBar(progress);
    }

    @OptionsItem(android.R.id.home)
    public void homeSelected() {
        app.startMainActivity();
    }
}
