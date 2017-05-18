package org.ovirt.mobile.movirt.ui.mvp;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.ui.HasProgressBar;

@EBean
public abstract class AccountDisposablesProgressBarPresenter<Presenter extends AccountDisposablesProgressBarPresenter,
        View extends FinishableProgressBarView> extends AccountDisposablesPresenter<Presenter, View> implements AccountPresenter, HasProgressBar {

    @Override
    public void showProgressBar() {
        final View view = getView();
        if (view != null) {
            view.showProgressBar();
        }
    }

    @Override
    public void hideProgressBar() {
        final View view = getView();
        if (view != null) {
            view.hideProgressBar();
        }
    }
}
