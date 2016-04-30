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

import java.text.DecimalFormat;
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

    private DecimalFormat decimalFormat = new DecimalFormat("0.#");

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
                renderCpuPercentageCircle(hostList);
                renderMemoryPercentageCircle(hostList);

                break;
            case STORAGE_DOMAIN_LOADER:
                List<StorageDomain> storageDomainList = EntityMapper.forEntity(StorageDomain.class).listFromCursor(data);
                List<StorageObject> storageObjectList = new ArrayList<>();

                for (StorageDomain sd : storageDomainList) {
                    StorageObject so = new StorageObject(sd.getAvailableSizeMb(), sd.getUsedSizeMb());
                    storageObjectList.add(so);
                }

                renderStoragePercentageCircle(storageObjectList);
                break;
            case VM_LOADER:
                List<Vm> vmList = vmFacade.mapAllFromCursor(data);
                renderCpuPercentageCircle(vmList);
                renderMemoryPercentageCircle(vmList);
                break;
            case DISK_LOADER:
                List<Disk> diskList = diskFacade.mapAllFromCursor(data);
                renderStoragePercentageCircle(diskList);
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
        long totalCpuUsage = 100;
        long usedCpuUsage = 0;

        for (T entity : entityList) {
            usedCpuUsage += entity.getCpuUsage();
        }
        if (entityList.size() > 0) {
            usedCpuUsage = usedCpuUsage / entityList.size();
        }

        cpuPercentageCircle.setMaxPercentageValue(totalCpuUsage);
        cpuPercentageCircle.setPercentageValue(usedCpuUsage);
        cpuPercentageCircle.setSummary(getString(R.string.used));
        cpuPercentageCircle.setNumberUnits("%");
        summaryCpuPercentageCircle.setText(getString(
                R.string.summary_cpu_percentage_circle, totalCpuUsage - usedCpuUsage, totalCpuUsage));

        cpuPercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction() && cpuPercentageCircle.isActivated()) {
                    startMainActivity(MainActivityFragments.HOSTS, OVirtContract.Host.CPU_USAGE, SortOrder.DESCENDING);
                }
                return false;
            }
        });
    }

    private <T extends OVirtContract.HasMemory> void renderMemoryPercentageCircle(List<T> entityList) {
        long memorySizeMb = 0;
        long usedMemorySizeMb = 0;

        for (T entity : entityList) {
            long memSizeMb = entity.getMemorySizeMb();

            if (memSizeMb > 0) {
                memorySizeMb += memSizeMb;
                usedMemorySizeMb += memSizeMb * entity.getMemoryUsage() / 100;
            }
        }

        if (memorySizeMb < 1024) {
            memoryPercentageCircle.setMaxPercentageValue(memorySizeMb);
            memoryPercentageCircle.setPercentageValue(usedMemorySizeMb);
            memoryPercentageCircle.setSummary(getString(R.string.mb_used));
            summaryMemoryPercentageCircle.setText(getString(
                    R.string.summary_memory_percentage_circle_mb, memorySizeMb - usedMemorySizeMb, memorySizeMb));
        } else {
            memoryPercentageCircle.setMaxPercentageValue(memorySizeMb / 1024f);
            memoryPercentageCircle.setPercentageValue(usedMemorySizeMb / 1024f, decimalFormat);
            memoryPercentageCircle.setSummary(getString(R.string.gb_used));
            summaryMemoryPercentageCircle.setText(getString(R.string.summary_memory_percentage_circle_gb,
                    decimalFormat.format((memorySizeMb - usedMemorySizeMb) / 1024f), decimalFormat.format(memorySizeMb / 1024f)));
        }

        memoryPercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction() && memoryPercentageCircle.isActivated()) {
                    startMainActivity(MainActivityFragments.HOSTS, OVirtContract.Host.MEMORY_USAGE, SortOrder.DESCENDING);
                }
                return false;
            }
        });
    }

    private <T extends OVirtContract.HasSizeMb & OVirtContract.HasUsedSizeMb> void renderStoragePercentageCircle(List<T> entityList) {
        long availableStorageSizeMb = 0;
        long usedStorageSizeMb = 0;

        for (T entity : entityList) {
            long size = entity.getSizeMb();
            long usedSze = entity.getUsedSizeMb();

            if (size > 0) {
                availableStorageSizeMb += size;
                if (usedSze > 0) {
                    usedStorageSizeMb += usedSze;
                }
            }
        }

        storagePercentageCircle.setMaxPercentageValue((availableStorageSizeMb + usedStorageSizeMb) / 1024f);
        storagePercentageCircle.setPercentageValue(usedStorageSizeMb / 1024f, decimalFormat);
        storagePercentageCircle.setSummary(getString(R.string.gb_used));
        summaryStoragePercentageCircle.setText(getString(R.string.summary_storage_percentage_circle,
                decimalFormat.format(availableStorageSizeMb / 1024f), decimalFormat.format((availableStorageSizeMb + usedStorageSizeMb) / 1024f)));

        storagePercentageCircle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // disable all touch events on storage
            }
        });
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

    private class StorageObject implements OVirtContract.HasSizeMb, OVirtContract.HasUsedSizeMb {
        private long sizeMb;
        private long usedSizeMb;

        public StorageObject(long sizeMb, long usedSizeMb) {
            this.sizeMb = sizeMb;
            this.usedSizeMb = usedSizeMb;
        }

        public long getSizeMb() {
            return sizeMb;
        }

        public void setSizeMb(long sizeMb) {
            this.sizeMb = sizeMb;
        }

        public long getUsedSizeMb() {
            return usedSizeMb;
        }

        public void setUsedSizeMb(long usedSizeMb) {
            this.usedSizeMb = usedSizeMb;
        }
    }
}
