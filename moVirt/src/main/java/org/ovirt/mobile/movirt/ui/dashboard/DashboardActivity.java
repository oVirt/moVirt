package org.ovirt.mobile.movirt.ui.dashboard;

import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.MoVirtActivity;

@EActivity(R.layout.activity_dashboard)
public class DashboardActivity extends MoVirtActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();

    private static final long SYNC_PERIOD_MILLIS = 3 * 60 * 1000;

    @ViewById
    ProgressBar progress;

    private Handler handler = new Handler();

    @AfterViews
    void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setProgressBar(progress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.post(syncRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(syncRunnable);
    }

    private Runnable syncRunnable = new Runnable( ) {
        public void run ( ) {
            Log.d(TAG, "Sync data");

            onRefresh();

            handler.postDelayed(syncRunnable, SYNC_PERIOD_MILLIS);
        }
    };
}
