package org.ovirt.mobile.movirt.ui.dashboard.generalfragment.virtual;

import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.DashboardGeneralFragment;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.PercentageCircleView;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.StartActivityAction;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;

@EFragment(R.layout.fragment_dashboard_virtual_general)
public class DashboardVirtualGeneralFragment extends DashboardGeneralFragment implements VirtualDashboardContract.View {

    @ViewById
    TextView summaryMemoryPercentageCircle;
    @ViewById
    TextView summaryCpuPercentageCircle;
    @ViewById
    TextView summaryStoragePercentageCircle;

    @ViewById
    PercentageCircleView cpuPercentageCircle;
    @ViewById
    PercentageCircleView memoryPercentageCircle;
    @ViewById
    PercentageCircleView storagePercentageCircle;

    private VirtualDashboardContract.Presenter presenter;

    @AfterViews
    void init() {
        presenter = VirtualDashboardPresenter_.getInstance_(getActivity().getApplicationContext())
                .setView(this)
                .initialize();
    }

    @Override
    public BasePresenter getPresenter() {
        return presenter;
    }

    @Override
    public void renderCpuPercentageCircle(UtilizationResource resource, StartActivityAction action) {
        renderCpuPercentageCircle(cpuPercentageCircle, summaryCpuPercentageCircle, resource, action);
    }

    @Override
    public void renderMemoryPercentageCircle(UtilizationResource resource, StartActivityAction action) {
        renderMemoryPercentageCircle(memoryPercentageCircle, summaryMemoryPercentageCircle, resource, action);
    }

    @Override
    public void renderStoragePercentageCircle(UtilizationResource resource, StartActivityAction action) {
        renderStoragePercentageCircle(storagePercentageCircle, summaryStoragePercentageCircle, resource, action);
    }
}
