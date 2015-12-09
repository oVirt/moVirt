package org.ovirt.mobile.movirt.ui;

import android.support.v7.app.ActionBarActivity;

import org.androidannotations.annotations.EActivity;

@EActivity
public abstract class ActionBarLoaderActivity extends ActionBarActivity implements HasLoader {

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
