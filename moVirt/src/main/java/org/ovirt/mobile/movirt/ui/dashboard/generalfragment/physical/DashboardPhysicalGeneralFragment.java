package org.ovirt.mobile.movirt.ui.dashboard.generalfragment.physical;

import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.DashboardGeneralFragment;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.PercentageCircleView;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.StartActivityAction;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.OverCommitResource;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;

@EFragment(R.layout.fragment_dashboard_physical_general)
public class DashboardPhysicalGeneralFragment extends DashboardGeneralFragment implements PhysicalDashboardContract.View {

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

    @ViewById
    TextView overCommitCpuPercentageCircle;
    @ViewById
    TextView overCommitMemoryPercentageCircle;

    private PhysicalDashboardContract.Presenter presenter;

    @AfterViews
    void init() {
        presenter = PhysicalDashboardPresenter_.getInstance_(getActivity().getApplicationContext())
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

    @Override
    public void renderCpuOverCommit(OverCommitResource resource) {
        renderOverCommit(overCommitCpuPercentageCircle, resource);
    }

    @Override
    public void renderMemoryOverCommit(OverCommitResource resource) {
        renderOverCommit(overCommitMemoryPercentageCircle, resource);
    }

    private void renderOverCommit(TextView textView, OverCommitResource resource) {
        if (resource != null && resource.isInitialized()) {
            textView.setText(getString(R.string.over_commit_allocated, resource.getOvercommit(), resource.getAllocated()));
        }
    }
}
