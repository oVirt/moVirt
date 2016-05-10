package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.DiskFacade;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.facade.VmFacade;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.LoaderFragment;
import org.ovirt.mobile.movirt.ui.MainActivity;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.MemorySize;
import org.ovirt.mobile.movirt.util.Percentage;
import org.ovirt.mobile.movirt.util.UsageResource;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotEmbeddableEntity.SNAPSHOT_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.STATUS;

@EFragment(R.layout.fragment_dashboard_general)
public class DashboardGeneralFragment extends LoaderFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DashboardGeneralFragment.class.getSimpleName();

    private static final int HOST_LOADER = 1;
    private static final int STORAGE_DOMAIN_LOADER = 2;
    private static final int VM_LOADER = 3;
    private static final int DISK_LOADER = 4;

    @Bean
    ProviderFacade provider;
    @Bean
    HostFacade hostFacade;
    @Bean
    VmFacade vmFacade;
    @Bean
    DiskFacade diskFacade;

    @ViewById
    TextView overCommitCpuPercentageCircle;
    @ViewById
    PercentageCircleView cpuPercentageCircle;
    @ViewById
    TextView overCommitMemoryPercentageCircle;
    @ViewById
    TextView summaryMemoryPercentageCircle;
    @ViewById
    PercentageCircleView memoryPercentageCircle;
    @ViewById
    TextView overCommitStoragePercentageCircle;
    @ViewById
    TextView summaryStoragePercentageCircle;
    @ViewById
    PercentageCircleView storagePercentageCircle;

    private UtilizationResource cpuResource;
    private UtilizationResource cpuVirtualResource;
    private UtilizationResource memoryResource;
    private UtilizationResource memoryVirtualResource;
    private UtilizationResource storageResource;
    private UtilizationResource storageVirtualResource;

    @AfterViews
    void init() {
        initLoaders();
    }

    private void initLoaders() {
        for (int loader : getLoaders()) {
            getLoaderManager().initLoader(loader, null, this);
        }
    }

    private int[] getLoaders() {
        return new int[]{VM_LOADER, DISK_LOADER, HOST_LOADER, STORAGE_DOMAIN_LOADER};
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Loader<Cursor> loader = null;

        switch (id) {
            case HOST_LOADER:
                loader = provider.query(Host.class).asLoader();
                break;
            case STORAGE_DOMAIN_LOADER:
                loader = provider.query(StorageDomain.class).where(StorageDomain.TYPE, StorageDomain.Type.DATA.toString()).asLoader();
                break;
            case VM_LOADER:
                loader = provider.query(Vm.class).where(SNAPSHOT_ID, "").where(STATUS, Vm.Status.UP.toString()).asLoader();
                break;
            case DISK_LOADER:
                loader = provider.query(Disk.class).where(SNAPSHOT_ID, "").asLoader();
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
                List<Host> hostList = hostFacade.mapAllFromCursor(data);
                cpuResource = getCpuUtilization(hostList);
                memoryResource = getMemoryUtilization(hostList);
                renderCpuPercentageCircle();
                renderMemoryPercentageCircle();
                break;
            case STORAGE_DOMAIN_LOADER:
                List<StorageDomain> storageDomainList = EntityMapper.forEntity(StorageDomain.class).listFromCursor(data);
                storageResource = getStorageDomainUtilization(storageDomainList);
                renderStoragePercentageCircle();
                break;
            case VM_LOADER:
                List<Vm> vmList = vmFacade.mapAllFromCursor(data);
                cpuVirtualResource = getCpuUtilization(vmList);
                memoryVirtualResource = getMemoryUtilization(vmList);
                renderCpuPercentageCircle();
                renderMemoryPercentageCircle();
                break;
            case DISK_LOADER:
                List<Disk> diskList = diskFacade.mapAllFromCursor(data);
                storageVirtualResource = getDisksUtilization(diskList);
                renderStoragePercentageCircle();
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @Override
    public void restartLoader() {
        for (int loader : getLoaders()) {
            getLoaderManager().restartLoader(loader, null, this);
        }
    }

    @Override
    public void destroyLoader() {
        for (int loader : getLoaders()) {
            getLoaderManager().destroyLoader(loader);
        }
    }


    private void renderCpuPercentageCircle() {
        renderOverCommit(overCommitCpuPercentageCircle, cpuResource, cpuVirtualResource);

        UtilizationResource resource = getVirtualViewState() ? cpuVirtualResource : cpuResource;
        if (resource == null) {
            return;
        }

        renderPercentageCircleView(cpuPercentageCircle, resource, false);

        cpuPercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction() && cpuPercentageCircle.isActivated()) {
                    if (getVirtualViewState()) {
                        startMainActivity(MainActivityFragments.VMS, OVirtContract.Vm.CPU_USAGE, SortOrder.DESCENDING);
                    } else {
                        startMainActivity(MainActivityFragments.HOSTS, OVirtContract.Host.CPU_USAGE, SortOrder.DESCENDING);
                    }
                }
                return false;
            }
        });
    }

    private void renderMemoryPercentageCircle() {
        renderOverCommit(overCommitMemoryPercentageCircle, memoryResource, memoryVirtualResource);

        UtilizationResource resource = getVirtualViewState() ? memoryVirtualResource : memoryResource;
        if (resource == null) {
            return;
        }

        renderPercentageCircleView(memoryPercentageCircle, resource, true);
        renderSummary(summaryMemoryPercentageCircle, resource);

        memoryPercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction() && memoryPercentageCircle.isActivated()) {
                    if (getVirtualViewState()) {
                        startMainActivity(MainActivityFragments.VMS, OVirtContract.Vm.MEMORY_USAGE, SortOrder.DESCENDING);
                    } else {
                        startMainActivity(MainActivityFragments.HOSTS, OVirtContract.Host.MEMORY_USAGE, SortOrder.DESCENDING);
                    }
                }
                return false;
            }
        });
    }


    private void renderStoragePercentageCircle() {
        renderOverCommit(overCommitStoragePercentageCircle, storageResource, storageVirtualResource);

        UtilizationResource resource = getVirtualViewState() ? storageVirtualResource : storageResource;
        if (resource == null) {
            return;
        }

        renderPercentageCircleView(storagePercentageCircle, resource, true);
        renderSummary(summaryStoragePercentageCircle, resource);

        storagePercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // disable all touch events on storage
            }
        });
    }

    private void renderOverCommit(TextView textView, UtilizationResource resource, UtilizationResource virtualResource) {
        if (resource != null && virtualResource != null) {
            double resourceTotal = (double) resource.getTotal().getValue();
            if (resourceTotal == 0) {
                return;
            }
            long overCommit = (long) (virtualResource.getUsed().getValue() / resourceTotal * 100);
            long allocated = (long) (virtualResource.getTotal().getValue() / resourceTotal * 100);

            textView.setText(getString(R.string.over_commit_allocated, overCommit, allocated));
        }
    }

    private void renderPercentageCircleView(PercentageCircleView circleView, UtilizationResource resource, boolean isMemoryUnit) {
        circleView.setMaxResource(resource.getTotal());
        circleView.setUsedResource(resource.getUsed());

        String resourceDescription = isMemoryUnit ?
                getString(R.string.unit_used, resource.getTotal().getReadableUnitAsString()) : getString(R.string.used);
        circleView.setUsedResourceDescription(resourceDescription);
    }

    private void renderSummary(TextView textView, UtilizationResource resource) {
        MemorySize totalResource = (MemorySize) resource.getTotal();
        MemorySize availableResource = (MemorySize) resource.getAvailable();

        String availableText = availableResource.getReadableValueAsString(totalResource.getReadableUnit());
        String totalText = totalResource.getReadableValueAsString();

        String summary = getString(R.string.summary_available_of, availableText, totalText,
                totalResource.getReadableUnitAsString());

        textView.setText(summary);

        // compute size of the text based on the string length,
        int stringLength = availableText.length() + totalText.length();
        int textLength;

        // auto-resizing hack
        switch (stringLength) {
            // 12 is maximum stringLength
            case 12:
                textLength = 12;
                break;
            // length 11 doesn't occur
            case 10:
            case 9:
                textLength = 13;
                break;
            // other lengths can be displayed with default size 14sp
            default:
                textLength = 14;
                break;
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textLength);
    }

    private <T extends OVirtContract.HasCpuUsage> UtilizationResource getCpuUtilization(List<T> entityList) {
        Percentage total = new Percentage(100);
        Percentage used = new Percentage();
        double usedTmp = 0;

        for (T entity : entityList) {
            usedTmp += entity.getCpuUsage();
        }

        if (entityList.size() > 0) {
            used.setValue((long) (usedTmp / entityList.size()));
        }

        return new UtilizationResource(used, total);
    }

    private <T extends OVirtContract.HasMemory> UtilizationResource getMemoryUtilization(List<T> entityList) {
        MemorySize total = new MemorySize();
        MemorySize used = new MemorySize();
        MemorySize available;

        for (T entity : entityList) {
            long memSize = entity.getMemorySize();
            total.addValue(memSize);
            used.addValue((long) (memSize * entity.getMemoryUsage() / 100));
        }

        available = new MemorySize(total.getValue() - used.getValue());

        return new UtilizationResource(used, total, available);
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


    public boolean getVirtualViewState() {
        FragmentActivity activity = getActivity();
        return activity != null && activity instanceof DashboardActivity && ((DashboardActivity) activity).getVirtualViewState();
    }

    public void render() {
        renderCpuPercentageCircle();
        renderMemoryPercentageCircle();
        renderStoragePercentageCircle();
    }

    private void startMainActivity(MainActivityFragments fragment, String orderBy, SortOrder order) {
        Intent intent = new Intent(getActivity(), MainActivity_.class);
        intent.setAction(fragment.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.Extras.FRAGMENT.name(), fragment);
        intent.putExtra(MainActivity.Extras.ORDER_BY.name(), orderBy);
        intent.putExtra(MainActivity.Extras.ORDER.name(), order);
        startActivity(intent);
    }

    private class UtilizationResource {
        private UsageResource available;
        private UsageResource used;
        private UsageResource total;

        public UtilizationResource(UsageResource used, UsageResource total) {
            this.used = used;
            this.total = total;
        }

        public UtilizationResource(UsageResource used, UsageResource total, UsageResource available) {
            this.used = used;
            this.total = total;
            this.available = available;
        }

        public UsageResource getUsed() {
            return used;
        }

        public void setUsed(UsageResource used) {
            this.used = used;
        }

        public UsageResource getTotal() {
            return total;
        }

        public void setTotal(UsageResource total) {
            this.total = total;
        }

        public UsageResource getAvailable() {
            return available;
        }

        public void setAvailable(UsageResource available) {
            this.available = available;
        }
    }
}
