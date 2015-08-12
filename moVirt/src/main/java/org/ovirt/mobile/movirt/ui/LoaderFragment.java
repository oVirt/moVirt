package org.ovirt.mobile.movirt.ui;

import android.support.v4.app.Fragment;
import org.androidannotations.annotations.EFragment;

@EFragment
public abstract class LoaderFragment extends Fragment implements HasLoader {

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
