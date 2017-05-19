package org.ovirt.mobile.movirt.ui.dashboard.generalfragment.physical;

import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.StartActivityAction;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.OverCommitResource;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;
import org.ovirt.mobile.movirt.ui.mvp.BaseView;

public interface PhysicalDashboardContract {

    interface View extends BaseView {
        void renderCpuPercentageCircle(UtilizationResource resource, StartActivityAction action);

        void renderMemoryPercentageCircle(UtilizationResource resource, StartActivityAction action);

        void renderStoragePercentageCircle(UtilizationResource resource, StartActivityAction action);

        void renderCpuOverCommit(OverCommitResource resource);

        void renderMemoryOverCommit(OverCommitResource resource);
    }

    interface Presenter extends BasePresenter {
    }
}

