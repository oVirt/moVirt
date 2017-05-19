package org.ovirt.mobile.movirt.ui.dashboard.boxfragment;

import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;

public interface DashboardBoxContract {

    interface View extends BaseView {
        void putData(DashboardBoxData data);
    }

    interface Presenter extends BasePresenter {
    }
}

