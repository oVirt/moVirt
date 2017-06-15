package org.ovirt.mobile.movirt.ui;

import org.androidannotations.annotations.EActivity;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;

@EActivity
public abstract class PresenterBroadcastAwareActivity extends BroadcastAwareAppCompatActivity implements BaseView {

    @Override
    public abstract BasePresenter getPresenter();

    @Override
    protected void onStop() {
        if (isFinishing()) {
            final BasePresenter presenter = getPresenter();
            if (presenter != null) {
                presenter.destroy();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        final BasePresenter presenter = getPresenter();
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroy();
    }
}
