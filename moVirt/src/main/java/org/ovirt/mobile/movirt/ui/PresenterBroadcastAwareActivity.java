package org.ovirt.mobile.movirt.ui;

import org.androidannotations.annotations.EActivity;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;

@EActivity
public abstract class PresenterBroadcastAwareActivity extends BroadcastAwareAppCompatActivity {

    protected abstract BasePresenter getPresenter();

    @Override
    protected void onStop() {
        if (isFinishing()) {
            if (getPresenter() != null) {
                getPresenter().destroy();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (getPresenter() != null) {
            getPresenter().destroy();
        }
        super.onDestroy();
    }
}
