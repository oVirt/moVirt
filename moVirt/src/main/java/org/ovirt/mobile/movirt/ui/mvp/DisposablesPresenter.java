package org.ovirt.mobile.movirt.ui.mvp;

import org.ovirt.mobile.movirt.util.Disposables;

public abstract class DisposablesPresenter<P extends DisposablesPresenter, V extends BaseView> extends AbstractBasePresenter<P, V> {

    private Disposables disposables = new Disposables();

    protected Disposables getDisposables() {
        return disposables;
    }

    @Override
    public void destroy() {
        disposables.destroy();
    }
}
