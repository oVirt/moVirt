package org.ovirt.mobile.movirt.ui.dashboard.generalfragment.virtual;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.DashboardGeneralFragmentHelper;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.StartActivityAction;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;
import org.ovirt.mobile.movirt.util.usage.Cores;
import org.ovirt.mobile.movirt.util.usage.MemorySize;
import org.ovirt.mobile.movirt.util.usage.Percentage;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.ovirt.mobile.movirt.provider.OVirtContract.HasStatus.STATUS;
import static org.ovirt.mobile.movirt.ui.dashboard.DashboardHelper.querySelection;

@EBean
public class VirtualDashboardPresenter extends DisposablesPresenter<VirtualDashboardPresenter, VirtualDashboardContract.View>
        implements VirtualDashboardContract.Presenter {

    @Bean
    AccountRxStore rxStore;

    @Bean
    ProviderFacade providerFacade;

    @Override
    public VirtualDashboardPresenter initialize() {
        super.initialize();

        getDisposables().add(rxStore.ACTIVE_SELECTION
                .distinctUntilChanged()
                .switchMap(selection -> {
                    final Observable<List<Vm>> vms = querySelection(providerFacade, Vm.class, selection)
                            .where(STATUS, VmStatus.UP.toString())
                            .asObservable();

                    final Observable<List<Disk>> disks = querySelection(providerFacade, Disk.class, selection)
                            .asObservable();

                    return Observable.combineLatest(vms, disks, Wrapper::new);
                })
                .map(this::process)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::render));

        return this;
    }

    private ResultWrapper process(Wrapper data) {
        return new ResultWrapper(
                getCpuUtilization(data.vms),
                DashboardGeneralFragmentHelper.getMemoryUtilization(data.vms),
                getDisksUtilization(data.disks));
    }

    private void render(ResultWrapper data) {
        getView().renderCpuPercentageCircle(data.cpuResource,
                new StartActivityAction(MainActivityFragments.VMS, OVirtContract.Vm.CPU_USAGE, SortOrder.DESCENDING));

        getView().renderMemoryPercentageCircle(data.memoryResource,
                new StartActivityAction(MainActivityFragments.VMS, OVirtContract.Vm.MEMORY_USAGE, SortOrder.DESCENDING));

        getView().renderStoragePercentageCircle(data.storageResource,
                new StartActivityAction(MainActivityFragments.STORAGE_DOMAIN, OVirtContract.StorageDomain.STATUS, SortOrder.ASCENDING));
    }

    public static UtilizationResource getCpuUtilization(List<Vm> entities) {
        Cores allCores = new Cores();
        double usedPercentagesSum = 0;

        for (Vm entity : entities) {
            usedPercentagesSum += entity.getCpuUsage();
            allCores.addValue(entity);
        }

        Percentage used = new Percentage((long) usedPercentagesSum / (allCores.getValue() == 0 ? 1 : allCores.getValue()));
        Percentage total = new Percentage(100);
        Percentage available = new Percentage(total.getValue() - used.getValue());

        return new UtilizationResource(used, total, available);
    }

    private UtilizationResource getDisksUtilization(List<Disk> diskList) {
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

    private class ResultWrapper {
        final UtilizationResource cpuResource;
        final UtilizationResource memoryResource;
        final UtilizationResource storageResource;

        ResultWrapper(UtilizationResource cpuResource, UtilizationResource memoryResource, UtilizationResource storageResource) {
            this.cpuResource = cpuResource;
            this.memoryResource = memoryResource;
            this.storageResource = storageResource;
        }
    }

    private class Wrapper {
        final List<Vm> vms;
        final List<Disk> disks;

        Wrapper(List<Vm> vms, List<Disk> disks) {
            this.vms = vms;
            this.disks = disks;
        }
    }
}
