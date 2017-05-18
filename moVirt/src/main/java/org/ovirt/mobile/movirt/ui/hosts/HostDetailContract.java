package org.ovirt.mobile.movirt.ui.hosts;

import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.ui.mvp.AccountPresenter;
import org.ovirt.mobile.movirt.ui.mvp.FinishableProgressBarView;
import org.ovirt.mobile.movirt.ui.mvp.StatusView;

public interface HostDetailContract {

    interface View extends FinishableProgressBarView, StatusView {

        void displayHostStatus(HostStatus status);
    }

    interface Presenter extends AccountPresenter {
        Presenter setHostId(String id);

        void activate();

        void deactivate();
    }
}

