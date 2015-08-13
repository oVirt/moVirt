package org.ovirt.mobile.movirt.ui;

import org.androidannotations.annotations.EFragment;

@EFragment
public abstract class RefreshableLoaderFragment extends RefreshableFragment implements HasLoader {

    @Override
    public void onResume() {
        super.onResume();
        restartLoader();
    }

    @Override
    public void onPause() {
        super.onPause();
        destroyLoader();
    }
}
