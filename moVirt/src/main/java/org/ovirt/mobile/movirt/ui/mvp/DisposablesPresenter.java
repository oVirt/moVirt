package org.ovirt.mobile.movirt.ui.mvp;

import org.ovirt.mobile.movirt.util.Disposables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.disposables.Disposable;

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
