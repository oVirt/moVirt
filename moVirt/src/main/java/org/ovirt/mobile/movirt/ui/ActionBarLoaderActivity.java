package org.ovirt.mobile.movirt.ui;

import org.androidannotations.annotations.EActivity;

@EActivity
public abstract class ActionBarLoaderActivity extends BroadcastAwareAppCompatActivity implements HasLoader {

    @Override
    protected void onResume() {
        super.onResume();
        restartLoader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyLoader();
    }
}
