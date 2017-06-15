package org.ovirt.mobile.movirt.ui.storage;

import org.ovirt.mobile.movirt.ui.mvp.AccountPresenter;
import org.ovirt.mobile.movirt.ui.mvp.FinishableView;
import org.ovirt.mobile.movirt.ui.mvp.StatusView;

public interface StorageDomainDetailContract {

    interface View extends FinishableView, StatusView {
    }

    interface Presenter extends AccountPresenter {
        Presenter setStorageDomainId(String id);
    }
}

