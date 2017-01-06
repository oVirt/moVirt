package org.ovirt.mobile.movirt.ui.dashboard.general;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.DiskFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.dashboard.PercentageCircleView;
import org.ovirt.mobile.movirt.util.MemorySize;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotEmbeddableEntity.SNAPSHOT_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.STATUS;

@EFragment(R.layout.fragment_dashboard_virtual_general)
public class DashboardVirtualGeneralFragment extends DashboardGeneralFragment {
    private static final String TAG = DashboardVirtualGeneralFragment.class.getSimpleName();

    private static final int VM_LOADER = 1;
    private static final int DISK_LOADER = 2;

    @Bean
    ProviderFacade provider;

    @Bean
    VmFacade vmFacade;

    @Bean
    DiskFacade diskFacade;

    @AfterViews
    void init() {
        initLoaders();
    }

    @Override
    protected int[] getLoaders() {
        return new int[]{VM_LOADER, DISK_LOADER};
    }

    @ViewById
    public void summaryMemoryPercentageCircle(TextView summaryMemoryPercentageCircle) {
        super.summaryMemoryPercentageCircle = summaryMemoryPercentageCircle;
    }

    @ViewById
    public void summaryCpuPercentageCircle(TextView summaryCpuPercentageCircle) {
        super.summaryCpuPercentageCircle = summaryCpuPercentageCircle;
    }

    @ViewById
    public void summaryStoragePercentageCircle(TextView summaryStoragePercentageCircle) {
        super.summaryStoragePercentageCircle = summaryStoragePercentageCircle;
    }

    @ViewById
    public void cpuPercentageCircle(PercentageCircleView cpuPercentageCircle) {
        super.cpuPercentageCircle = cpuPercentageCircle;
    }

    @ViewById
    public void memoryPercentageCircle(PercentageCircleView memoryPercentageCircle) {
        super.memoryPercentageCircle = memoryPercentageCircle;
    }

    @ViewById
    public void storagePercentageCircle(PercentageCircleView storagePercentageCircle) {
        super.storagePercentageCircle = storagePercentageCircle;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Loader<Cursor> loader = null;

        switch (id) {
            case VM_LOADER:
                loader = provider.query(Vm.class).empty(SNAPSHOT_ID).where(STATUS, Vm.Status.UP.toString()).asLoader();
                break;
            case DISK_LOADER:
                loader = provider.query(Disk.class).empty(SNAPSHOT_ID).asLoader();
                break;
            default:
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case VM_LOADER:
                List<Vm> vmList = vmFacade.mapAllFromCursor(data);
                StartActivityAction cpuAction = new StartActivityAction(MainActivityFragments.VMS, OVirtContract.Vm.CPU_USAGE, SortOrder.DESCENDING);
                StartActivityAction memoryAction = new StartActivityAction(MainActivityFragments.VMS, OVirtContract.Vm.MEMORY_USAGE, SortOrder.DESCENDING);
                renderCpuPercentageCircle(getCpuUtilization(vmList).first, cpuAction);
                renderMemoryPercentageCircle(getMemoryUtilization(vmList), memoryAction);
                break;
            case DISK_LOADER:
                List<Disk> diskList = diskFacade.mapAllFromCursor(data);
                renderStoragePercentageCircle(getDisksUtilization(diskList), null);
                break;
            default:
                break;
        }
    }

    protected UtilizationResource getDisksUtilization(List<Disk> diskList) {
        MemorySize used = new MemorySize();
        MemorySize total = new MemorySize();
        MemorySize available;

        for (Disk disk : diskList) {
            total.addValue(disk.getSize());
            used.addValue(disk.getUsedSize());
        }

        available = new MemorySize(total.getValue() - used.getValue());

        return new UtilizationResource(used, total, available);
    }
}
