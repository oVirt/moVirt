package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
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

import java.util.ArrayList;
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
    TextView summaryCpuPercentageCircle;
    @ViewById
    PercentageCircleView cpuPercentageCircle;
    @ViewById
    TextView summaryMemoryPercentageCircle;
    @ViewById
    PercentageCircleView memoryPercentageCircle;
    @ViewById
    TextView summaryStoragePercentageCircle;
    @ViewById
    PercentageCircleView storagePercentageCircle;

    @InstanceState
    boolean virtualView = false;

    @AfterViews
    void init() {
        initLoaders();
    }

    private void initLoaders() {
        for (int loader : getActiveLoaders()) {
            getLoaderManager().initLoader(loader, null, this);
        }
    }

    private int[] getActiveLoaders() {
        return virtualView ? new int[]{VM_LOADER, DISK_LOADER} : new int[]{HOST_LOADER, STORAGE_DOMAIN_LOADER};
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
            case HOST_LOADER:
                List<Host> hostList = hostFacade.mapAllFromCursor(data);
                renderCpuPercentageCircle(hostList);
                renderMemoryPercentageCircle(hostList);

                break;
            case STORAGE_DOMAIN_LOADER:
                List<StorageDomain> storageDomainList = EntityMapper.forEntity(StorageDomain.class).listFromCursor(data);
                renderStoragePercentageCircle(storageDomainList);
                break;
            case VM_LOADER:
                List<Vm> vmList = vmFacade.mapAllFromCursor(data);
                renderCpuPercentageCircle(vmList);
                renderMemoryPercentageCircle(vmList);
                break;
            case DISK_LOADER:
                List<Disk> diskList = diskFacade.mapAllFromCursor(data);
                List<StorageObject> storageObjectList = new ArrayList<>();

                for (Disk d : diskList) {
                    StorageObject so = new StorageObject(d.getSize() - d.getUsedSize(), d.getUsedSize());
                    storageObjectList.add(so);
                }

                renderStoragePercentageCircle(storageObjectList);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    private <T extends OVirtContract.HasCpuUsage> void renderCpuPercentageCircle(List<T> entityList) {
        Percentage totalCpuUsage = new Percentage(100);
        Percentage usedCpuUsage = new Percentage();
        double usedCpuUsageTmp = 0;

        for (T entity : entityList) {
            usedCpuUsageTmp += entity.getCpuUsage();
        }

        if (entityList.size() > 0) {
            usedCpuUsage.setValue((long) (usedCpuUsageTmp / entityList.size()));
        }

        cpuPercentageCircle.setMaxResource(totalCpuUsage);
        cpuPercentageCircle.setUsedResource(usedCpuUsage);
        cpuPercentageCircle.setUsedResourceDescription(getString(R.string.used));
        String summary = getString(R.string.summary_available_of,
                totalCpuUsage.getValue() - usedCpuUsage.getValue(),
                totalCpuUsage.getReadableValueAsString(),
                totalCpuUsage.getReadableUnitAsString());
        summaryCpuPercentageCircle.setText(summary);

        cpuPercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction() && cpuPercentageCircle.isActivated()) {
                    if (virtualView) {
                        startMainActivity(MainActivityFragments.VMS, OVirtContract.Vm.CPU_USAGE, SortOrder.DESCENDING);
                    } else {
                        startMainActivity(MainActivityFragments.HOSTS, OVirtContract.Host.CPU_USAGE, SortOrder.DESCENDING);
                    }
                }
                return false;
            }
        });
    }

    private <T extends OVirtContract.HasMemory> void renderMemoryPercentageCircle(List<T> entityList) {
        MemorySize memory = new MemorySize();
        MemorySize usedMemory = new MemorySize();
        MemorySize availableMemory;

        for (T entity : entityList) {
            long memSize = entity.getMemorySize();
            memory.addValue(memSize);
            usedMemory.addValue((long) (memSize * entity.getMemoryUsage() / 100));
        }

        availableMemory = new MemorySize(memory.getValue() - usedMemory.getValue());

        memoryPercentageCircle.setMaxResource(memory);
        memoryPercentageCircle.setUsedResource(usedMemory);
        memoryPercentageCircle.setUsedResourceDescription(getString(R.string.unit_used, memory.getReadableUnitAsString()));
        summaryMemoryPercentageCircle.setText(getAvailableSummary(memory, availableMemory));

        memoryPercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction() && memoryPercentageCircle.isActivated()) {
                    if (virtualView) {
                        startMainActivity(MainActivityFragments.VMS, OVirtContract.Vm.MEMORY_USAGE, SortOrder.DESCENDING);
                    } else {
                        startMainActivity(MainActivityFragments.HOSTS, OVirtContract.Host.MEMORY_USAGE, SortOrder.DESCENDING);
                    }
                }
                return false;
            }
        });
    }

    private <T extends OVirtContract.HasAvailableSize & OVirtContract.HasUsedSize> void renderStoragePercentageCircle(List<T> entityList) {
        MemorySize availableStorage = new MemorySize();
        MemorySize usedStorage = new MemorySize();
        MemorySize storage;

        for (T entity : entityList) {
            availableStorage.addValue(entity.getAvailableSize());
            usedStorage.addValue(entity.getUsedSize());
        }

        storage = new MemorySize(availableStorage.getValue() + usedStorage.getValue());

        storagePercentageCircle.setMaxResource(storage);
        storagePercentageCircle.setUsedResource(usedStorage);
        storagePercentageCircle.setUsedResourceDescription(getString(R.string.unit_used, storage.getReadableUnitAsString()));
        summaryStoragePercentageCircle.setText(getAvailableSummary(storage, availableStorage));

        storagePercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // disable all touch events on storage
            }
        });
    }

    private String getAvailableSummary(MemorySize memory, MemorySize availableMemory) {
        return getString(R.string.summary_available_of,
                availableMemory.getReadableValueAsString(memory.getReadableUnit()),
                memory.getReadableValueAsString(),
                memory.getReadableUnitAsString());
    }

    @Override
    public void restartLoader() {
        for (int loader : getActiveLoaders()) {
            getLoaderManager().restartLoader(loader, null, this);
        }
    }

    @Override
    public void destroyLoader() {
        for (int loader : getActiveLoaders()) {
            getLoaderManager().destroyLoader(loader);
        }
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

    public boolean isVirtualView() {
        return virtualView;
    }

    public void setVirtualView(boolean virtualView) {
        this.virtualView = virtualView;
    }

    private class StorageObject implements OVirtContract.HasAvailableSize, OVirtContract.HasUsedSize {
        private long availableSize;
        private long usedSize;

        public StorageObject(long availableSize, long usedSize) {
            this.availableSize = availableSize;
            this.usedSize = usedSize;
        }

        public long getAvailableSize() {
            return availableSize;
        }

        public void setAvailableSize(long availableSize) {
            this.availableSize = availableSize;
        }

        public long getUsedSize() {
            return usedSize;
        }

        public void setUsedSize(long usedSize) {
            this.usedSize = usedSize;
        }
    }
}
