package org.ovirt.mobile.movirt.ui.dashboard.generalfragment.physical;

import android.support.v4.util.Pair;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.StorageDomainStatus;
import org.ovirt.mobile.movirt.model.enums.StorageDomainType;
import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.DashboardGeneralFragmentHelper;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.StartActivityAction;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.OverCommitResource;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;
import org.ovirt.mobile.movirt.util.usage.Cores;
import org.ovirt.mobile.movirt.util.usage.MemorySize;
import org.ovirt.mobile.movirt.util.usage.Percentage;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.ovirt.mobile.movirt.ui.dashboard.DashboardHelper.querySelection;

@EBean
public class PhysicalDashboardPresenter extends DisposablesPresenter<PhysicalDashboardPresenter, PhysicalDashboardContract.View>
        implements PhysicalDashboardContract.Presenter {

    @Bean
    AccountRxStore rxStore;

    @Bean
    ProviderFacade providerFacade;

    @Override
    public PhysicalDashboardPresenter initialize() {
        super.initialize();

        getDisposables().add(rxStore.ACTIVE_SELECTION
                .distinctUntilChanged()
                .switchMap(selection -> {

                    final Observable<List<Host>> hosts = querySelection(providerFacade, Host.class, selection).asObservable();
                    final Observable<List<Vm>> vms = querySelection(providerFacade, Vm.class, selection).asObservable();
                    final Observable<List<StorageDomain>> storages = querySelection(providerFacade, StorageDomain.class, selection)
                            .where(StorageDomain.STATUS, StorageDomainStatus.ACTIVE.toString())
                            .where(StorageDomain.TYPE, StorageDomainType.DATA.toString())
                            .asObservable();
                    return Observable.combineLatest(hosts, vms, storages, Wrapper::new);
                })
                .map(this::process)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::render));

        return this;
    }

    private ResultWrapper process(Wrapper data) {
        OverCommitResource cpuOverCommit = new OverCommitResource();
        Pair<UtilizationResource, Cores> cpuUtilization = getCpuUtilization(data.hosts);
        UtilizationResource cpuResource = cpuUtilization.first;
        cpuOverCommit.setPhysicalTotal(cpuUtilization.second);

        OverCommitResource memoryOverCommit = new OverCommitResource();
        UtilizationResource memoryResource = DashboardGeneralFragmentHelper.getMemoryUtilization(data.hosts);
        memoryOverCommit.setPhysicalTotal(memoryResource.getTotal());

        processVmCpuAndMemory(memoryOverCommit, cpuOverCommit, data.vms);

        UtilizationResource storageResource = getStorageDomainUtilization(data.storages);

        return new ResultWrapper(cpuResource, memoryResource, storageResource,
                cpuOverCommit, memoryOverCommit);
    }

    private void render(ResultWrapper data) {

        getView().renderCpuPercentageCircle(data.cpuResource,
                new StartActivityAction(MainActivityFragments.HOSTS, OVirtContract.Vm.CPU_USAGE, SortOrder.DESCENDING));
        getView().renderCpuOverCommit(data.cpuOverCommit);

        getView().renderMemoryPercentageCircle(data.memoryResource,
                new StartActivityAction(MainActivityFragments.HOSTS, OVirtContract.Vm.MEMORY_USAGE, SortOrder.DESCENDING));
        getView().renderMemoryOverCommit(data.memoryOverCommit);

        getView().renderStoragePercentageCircle(data.storageResource,
                new StartActivityAction(MainActivityFragments.STORAGE_DOMAIN, OVirtContract.StorageDomain.STATUS, SortOrder.ASCENDING));
    }

    private void processVmCpuAndMemory(OverCommitResource memoryOverCommit, OverCommitResource cpuOverCommit, List<Vm> vms) {

        Cores allVmCores = new Cores();
        Cores upVmCores = new Cores();

        MemorySize allVmMemory = new MemorySize();
        MemorySize upVmMemory = new MemorySize();

        for (Vm vm : vms) {
            if (vm.getStatus() == VmStatus.UP) {
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

    private static Pair<UtilizationResource, Cores> getCpuUtilization(List<Host> entities) {
        Cores allCores = new Cores();
        double usedPercentagesSum = 0;

        for (Host entity : entities) {
            Cores entityCores = new Cores(entity);

            usedPercentagesSum += entityCores.getValue() * entity.getCpuUsage();
            allCores.addValue(entityCores);
        }

        // average of all host usages
        Percentage used = new Percentage((long) usedPercentagesSum / (allCores.getValue() == 0 ? 1 : allCores.getValue()));
        Percentage total = new Percentage(100);
        Percentage available = new Percentage(total.getValue() - used.getValue());

        return new Pair<>(new UtilizationResource(used, total, available), allCores);
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

    private class ResultWrapper {
        final UtilizationResource cpuResource;
        final UtilizationResource memoryResource;
        final UtilizationResource storageResource;
        final OverCommitResource cpuOverCommit;
        final OverCommitResource memoryOverCommit;

        public ResultWrapper(UtilizationResource cpuResource, UtilizationResource memoryResource, UtilizationResource storageResource,
                             OverCommitResource cpuOverCommit, OverCommitResource memoryOverCommit) {
            this.cpuResource = cpuResource;
            this.memoryResource = memoryResource;
            this.storageResource = storageResource;
            this.cpuOverCommit = cpuOverCommit;
            this.memoryOverCommit = memoryOverCommit;
        }
    }

    private class Wrapper {
        final List<Host> hosts;
        final List<Vm> vms;
        final List<StorageDomain> storages;

        Wrapper(List<Host> hosts, List<Vm> vms, List<StorageDomain> storages) {
            this.hosts = hosts;
            this.vms = vms;
            this.storages = storages;
        }
    }
}
