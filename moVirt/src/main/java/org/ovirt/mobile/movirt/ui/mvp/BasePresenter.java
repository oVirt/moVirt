package org.ovirt.mobile.movirt.ui.mvp;

public interface BasePresenter {

    BasePresenter setView(BaseView view);

    BasePresenter initialize();

    void destroy();
}
