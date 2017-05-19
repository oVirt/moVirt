package org.ovirt.mobile.movirt.ui;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;

@EFragment
public abstract class PresenterFragment extends Fragment implements BaseView {

    @Override
    public abstract BasePresenter getPresenter();

    @Override
    public void onDestroyView() {
        final BasePresenter presenter = getPresenter();
        if (presenter != null) {
            presenter.destroy();
        }
        super.onDestroyView();
    }
}
