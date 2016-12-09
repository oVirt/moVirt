package org.ovirt.mobile.movirt.ui.dashboard;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.BooleanRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.dashboard.general.DashboardPhysicalGeneralFragment_;
import org.ovirt.mobile.movirt.ui.dashboard.general.DashboardVirtualGeneralFragment_;

@EFragment(R.layout.fragment_dasboard_container)
public class DashboardContainer extends Fragment {
    private static final String TAG = DashboardContainer.class.getSimpleName();

    @ViewById
    TextView tabletHeader;

    @InstanceState
    DashboardType dashboardType = DashboardType.PHYSICAL;

    @BooleanRes
    boolean isTablet;

    @AfterViews
    void init() {
        if (getView() == null) {
            return;
        }

        int generalId = dashboardType == DashboardType.PHYSICAL ? R.id.fragment_dashboard_general_physical_container
                : R.id.fragment_dashboard_general_virtual_container;
        getView().findViewById(R.id.fragment_dashboard_general_container).setId(generalId);

        Fragment general = dashboardType == DashboardType.PHYSICAL ? new DashboardPhysicalGeneralFragment_() :
                new DashboardVirtualGeneralFragment_();

        getFragmentManager().beginTransaction()
                .replace(generalId, general)
                .commit();

        if (isTablet) {
            int mostUtilizedId = dashboardType == DashboardType.PHYSICAL ? R.id.fragment_dashboard_most_utilized_physical_container
                    : R.id.fragment_dashboard_most_utilized_virtual_container;
            getView().findViewById(R.id.fragment_dashboard_most_utilized_container).setId(mostUtilizedId);

            DashboardMostUtilizedFragment mostUtilized = new DashboardMostUtilizedFragment_();
            mostUtilized.setDashboardType(dashboardType);

            getFragmentManager().beginTransaction()
                    .replace(mostUtilizedId, mostUtilized)
                    .commit();

            tabletHeader.setText(dashboardType == DashboardType.PHYSICAL ? R.string.physical_resources : R.string.running_virtual_machines);
        }
    }

    public void setDashboardType(DashboardType dashboardType) {
        this.dashboardType = dashboardType;
    }
}
