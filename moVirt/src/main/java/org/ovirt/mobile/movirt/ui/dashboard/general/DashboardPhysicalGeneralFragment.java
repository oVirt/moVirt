package org.ovirt.mobile.movirt.ui.dashboard.general;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.dashboard.PercentageCircleView;
import org.ovirt.mobile.movirt.util.Cores;
import org.ovirt.mobile.movirt.util.MemorySize;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotEmbeddableEntity.SNAPSHOT_ID;

@EFragment(R.layout.fragment_dashboard_physical_general)
public class DashboardPhysicalGeneralFragment extends DashboardGeneralFragment {
    private static final String TAG = DashboardPhysicalGeneralFragment.class.getSimpleName();

    private static final int HOST_LOADER = 1;
    private static final int STORAGE_DOMAIN_LOADER = 2;
    private static final int VM_LOADER = 3;

    @ViewById
    TextView overCommitCpuPercentageCircle;

    @ViewById
    TextView overCommitMemoryPercentageCircle;

    @Bean
    ProviderFacade provider;

    @Bean
    HostFacade hostFacade;

    @Bean
    VmFacade vmFacade;

    private UtilizationResource cpuResource;
    private OverCommitResource cpuOverCommit = new OverCommitResource();

    private UtilizationResource memoryResource;
    private OverCommitResource memoryOverCommit = new OverCommitResource();

    private UtilizationResource storageResource;

    @AfterViews
    void init() {
        initLoaders();
    }

    @Override
    protected int[] getLoaders() {
        return new int[]{VM_LOADER, HOST_LOADER, STORAGE_DOMAIN_LOADER};
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
            case HOST_LOADER:
                loader = provider.query(Host.class).asLoader();
                break;
            case STORAGE_DOMAIN_LOADER:
                loader = provider.query(StorageDomain.class).where(StorageDomain.STATUS, StorageDomain.Status.ACTIVE.toString())
                        .where(StorageDomain.TYPE, StorageDomain.Type.DATA.toString()).asLoader();
                break;
            case VM_LOADER:
                loader = provider.query(Vm.class).empty(SNAPSHOT_ID).asLoader();
                break;
            default:
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case HOST_LOADER:
                List<Host> hosts = hostFacade.mapAllFromCursor(data);
                processHostCpu(hosts);
                processHostMemory(hosts);

                renderCpuPercentageCircle();
                renderMemoryPercentageCircle();
                break;
            case STORAGE_DOMAIN_LOADER:
                List<StorageDomain> storageDomainList = EntityMapper.forEntity(StorageDomain.class).listFromCursor(data);
                storageResource = getStorageDomainUtilization(storageDomainList);

                renderStoragePercentageCircle();
                break;
            case VM_LOADER:
                List<Vm> vms = vmFacade.mapAllFromCursor(data);
                processVmCpuAndMemory(vms);

                renderCpuPercentageCircle();
                renderMemoryPercentageCircle();
                break;
            default:
                break;
        }
    }

    private void processVmCpuAndMemory(List<Vm> vms) {
        Cores allVmCores = new Cores();
        Cores upVmCores = new Cores();

        MemorySize allVmMemory = new MemorySize();
        MemorySize upVmMemory = new MemorySize();

        for (Vm vm : vms) {
            if (vm.getStatus() == Vm.Status.UP) {
                upVmCores.addValue(vm);
                upVmMemory.addValue(vm.getMemorySize());
            }
            allVmCores.addValue(vm);
            allVmMemory.addValue(vm.getMemorySize());
        }

        cpuOverCommit.setVirtualTotal(allVmCores);
        cpuOverCommit.setVirtualUsed(upVmCores);

        memoryOverCommit.setVirtualTotal(allVmMemory);
        memoryOverCommit.setVirtualUsed(upVmMemory);
    }

    private void processHostMemory(List<Host> hosts) {
        memoryResource = getMemoryUtilization(hosts);
        memoryOverCommit.setPhysicalTotal(memoryResource.getTotal());
    }

    private void processHostCpu(List<Host> hosts) {
        Pair<UtilizationResource, Cores> utilization = getCpuUtilization(hosts);
        cpuResource = utilization.first;
        cpuOverCommit.setPhysicalTotal(utilization.second);
    }

    private UtilizationResource getStorageDomainUtilization(List<StorageDomain> domainList) {
        MemorySize available = new MemorySize();
        MemorySize used = new MemorySize();
        MemorySize total;

        for (StorageDomain entity : domainList) {
            available.addValue(entity.getAvailableSize());
            used.addValue(entity.getUsedSize());
        }

        total = new MemorySize(available.getValue() + used.getValue());

        return new UtilizationResource(used, total, available);
    }

    private void renderCpuPercentageCircle() {
        StartActivityAction cpuAction = new StartActivityAction(MainActivityFragments.HOSTS, OVirtContract.Vm.CPU_USAGE, SortOrder.DESCENDING);
        super.renderCpuPercentageCircle(cpuResource, cpuAction);
        renderOverCommit(overCommitCpuPercentageCircle, cpuOverCommit);
    }

    private void renderMemoryPercentageCircle() {
        StartActivityAction memoryAction = new StartActivityAction(MainActivityFragments.HOSTS, OVirtContract.Vm.MEMORY_USAGE, SortOrder.DESCENDING);
        super.renderMemoryPercentageCircle(memoryResource, memoryAction);
        renderOverCommit(overCommitMemoryPercentageCircle, memoryOverCommit);
    }

    private void renderStoragePercentageCircle() {
        super.renderStoragePercentageCircle(storageResource, null);
        // No feasible way to get meaningful data from REST api to render over commit
    }

    private void renderOverCommit(TextView textView, OverCommitResource resource) {
        if (resource != null && resource.isInitialized()) {
            textView.setText(getString(R.string.over_commit_allocated, resource.getOvercommit(), resource.getAllocated()));
        }
    }
}
