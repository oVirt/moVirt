package org.ovirt.mobile.movirt.ui.dashboard.boxfragment;

import android.widget.GridView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.PresenterFragment;
import org.ovirt.mobile.movirt.ui.mvp.BasePresenter;

@EFragment(R.layout.fragment_dashboard_box)
public class DashboardBoxFragment extends PresenterFragment implements DashboardBoxContract.View {

    @ViewById
    GridView boxGridView;

    @Bean
    ProviderFacade provider;

    DashboardBoxListAdapter dashboardBoxListAdapter;

    private DashboardBoxContract.Presenter presenter;

    @AfterViews
    void init() {
        dashboardBoxListAdapter = new DashboardBoxListAdapter(getActivity());
        boxGridView.setAdapter(dashboardBoxListAdapter);

        presenter = DashboardBoxPresenter_.getInstance_(getActivity().getApplicationContext())
                .setView(this)
                .initialize();
    }

    @Override
    public BasePresenter getPresenter() {
        return presenter;
    }

    @Override
    public void putData(DashboardBoxData data) {
        dashboardBoxListAdapter.putData(data);
    }
}
