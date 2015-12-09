package org.ovirt.mobile.movirt.ui.dashboard;

import android.view.WindowManager;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.MovirtActivity;

@EActivity(R.layout.activity_dashboard)
public class DashboardActivity extends MovirtActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();

    @ViewById
    ProgressBar progress;

    @AfterViews
    void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setProgressBar(progress);
    }

}
