package org.ovirt.mobile.movirt.ui.triggers;

import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;
import org.ovirt.mobile.movirt.ui.mvp.StatusView;

import java.util.List;

public interface EditTriggersContract {

    interface View extends BaseView, StatusView {

        void showTriggers(List<ViewTrigger> triggers);

        void startEditTriggerActivity(ViewTrigger trigger);

        void startAddTriggerActivity(Selection selection);
    }

    interface Presenter extends BasePresenter {

        void triggerSelected(ViewTrigger trigger);

        void triggerClicked(ViewTrigger trigger);

        void deleteTrigger(ViewTrigger trigger);

        void addTrigger();
    }
}

