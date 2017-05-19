package org.ovirt.mobile.movirt.ui.dashboard;

import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;
import org.ovirt.mobile.movirt.ui.mvp.StatusView;

public interface DashboardContract {

    interface View extends BaseView, StatusView {

    }

    interface Presenter extends BasePresenter {
    }
}

