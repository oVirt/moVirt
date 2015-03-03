package org.ovirt.mobile.movirt.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.SyncAdapter;

@EFragment(R.layout.fragment_vm_detail_general)
public class VmDetailGeneralFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, HasProgressBar {

    private static final String TAG = VmDetailGeneralFragment.class.getSimpleName();

    private static final String VM_URI = "vm_uri";

    private String vmId = null;

    @ViewById
    TextView statusView;

    @ViewById
    TextView cpuView;

    @ViewById
    TextView memView;

    @ViewById
    TextView memoryView;

    @ViewById
    TextView socketView;

    @ViewById
    TextView coreView;

    @ViewById
    TextView osView;

    @ViewById
    TextView displayView;

    @ViewById
    ProgressBar vncProgress;

    Bundle args;

    @Bean
    OVirtClient client;

    @Bean
    ProviderFacade provider;

    @Bean
    SyncAdapter syncAdapter;

    @StringRes(R.string.details_for_vm)
    String VM_DETAILS;

    Vm vm;

    @ViewById
    SwipeRefreshLayout swipeGeneralContainer;

    @AfterViews
    void initLoader() {
        swipeGeneralContainer.setOnRefreshListener(this);

        hideProgressBar();
        Uri vmUri = getActivity().getIntent().getData();

        args = new Bundle();
        args.putParcelable(VM_URI, vmUri);
        getLoaderManager().initLoader(0, args, this);
        vmId = vmUri.getLastPathSegment();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, args, this);
    }

    @UiThread
    @Background
    public void showProgressBar() {
        vncProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    @Background
    public void hideProgressBar() {
        vncProgress.setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String vmId = args.<Uri>getParcelable(VM_URI).getLastPathSegment();
        return provider.query(Vm.class).id(vmId).asLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Vm");
            return;
        }
        vm = EntityMapper.VM_MAPPER.fromCursor(data);
        renderVm(vm);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @UiThread
    public void renderVm(Vm vm) {
        getActivity().setTitle(String.format(VM_DETAILS, vm.getName()));
        statusView.setText(vm.getStatus().toString().toLowerCase());
        cpuView.setText(String.format("%.2f%%", vm.getCpuUsage()));
        memView.setText(String.format("%.2f%%", vm.getMemoryUsage()));
        if(vm.getMemorySizeMb() != -1) {
            memoryView.setText(vm.getMemorySizeMb() + " MB");
        }
        else {
            memoryView.setText("N/A");
        }
        socketView.setText(String.valueOf(vm.getSockets()));
        coreView.setText(String.valueOf(vm.getCoresPerSocket()));
        osView.setText(vm.getOsType());
        if (vm.getDisplayType() != null) {
            displayView.setText(vm.getDisplayType().toString());
        }
        else {
            displayView.setText("N/A");
        }

    }

    @Override
    @Background
    public void onRefresh() {
        syncAdapter.syncVm(vmId, new ProgressBarResponse<Vm>(this));
    }
}
