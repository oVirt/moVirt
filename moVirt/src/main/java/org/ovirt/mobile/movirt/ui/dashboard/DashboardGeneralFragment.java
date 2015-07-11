package org.ovirt.mobile.movirt.ui.dashboard;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.TextView;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.HostFacade;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_dashboard_general)
public class DashboardGeneralFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DashboardGeneralFragment.class.getSimpleName();

    private static final int HOST_LOADER = 1;
    private static final int STORAGE_DOMAIN_LOADER = 2;

    @Bean
    ProviderFacade provider;
    @Bean
    HostFacade hostFacade;

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

    private List<Host> hostList = new ArrayList<>();
    private List<StorageDomain> storageDomainList = new ArrayList<>();

    @AfterViews
    void init(){
        getLoaderManager().initLoader(HOST_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(HOST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Loader<Cursor> loader = null;

        switch (id) {
            case HOST_LOADER:
                loader = provider.query(Host.class).asLoader();;
                break;
            case STORAGE_DOMAIN_LOADER:
                loader = provider.query(StorageDomain.class).where(StorageDomain.TYPE, StorageDomain.Type.DATA.toString()).asLoader();
                break;
            default:
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading data: id=" + loader.getId());
            return;
        }

        switch (loader.getId()) {
            case HOST_LOADER:
                hostList.clear();
                do {
                    Host host = hostFacade.mapFromCursor(data);
                    hostList.add(host);
                } while (data.moveToNext());

                renderCpuPercentageCircle(hostList);
                renderMemoryPercentageCircle(hostList);

                if (getLoaderManager().getLoader(STORAGE_DOMAIN_LOADER) == null) {
                    getLoaderManager().initLoader(STORAGE_DOMAIN_LOADER, null, this);
                } else {
                    getLoaderManager().restartLoader(STORAGE_DOMAIN_LOADER, null, this);
                }

                break;
            case STORAGE_DOMAIN_LOADER:
                storageDomainList.clear();
                do {
                    StorageDomain storageDomain = EntityMapper.forEntity(StorageDomain.class).fromCursor(data);
                    storageDomainList.add(storageDomain);
                } while (data.moveToNext());

                renderStoragePercentageCircle(storageDomainList);

                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    private void renderCpuPercentageCircle(List<Host> hostList) {
        long cpuSpeed = 0;
        long usedCpuSpeed = 0;
        for (Host host : hostList) {
            cpuSpeed += host.getCpuSpeed();
            usedCpuSpeed += host.getCpuSpeed() * host.getCpuUsage() / 100;
        }
        cpuPercentageCircle.setMaxPercentageValue(cpuSpeed);
        cpuPercentageCircle.setPercentageValue(usedCpuSpeed);
        cpuPercentageCircle.setSummary(getString(R.string.mhz_used));
        summaryCpuPercentageCircle.setText(getString(
                R.string.summary_cpu_percentage_circle, cpuSpeed - usedCpuSpeed, cpuSpeed));
    }

    private void renderMemoryPercentageCircle(List<Host> hostList) {
        long memorySizeMb = 0;
        long usedMemorySizeMb = 0;
        for (Host host : hostList) {
            memorySizeMb += host.getMemorySizeMb();
            usedMemorySizeMb += host.getMemorySizeMb() * host.getMemoryUsage() / 100;
        }
        memoryPercentageCircle.setMaxPercentageValue(memorySizeMb);
        memoryPercentageCircle.setPercentageValue(usedMemorySizeMb);
        memoryPercentageCircle.setSummary(getString(R.string.mb_used));
        summaryMemoryPercentageCircle.setText(getString(
                R.string.summary_memory_percentage_circle, memorySizeMb - usedMemorySizeMb, memorySizeMb));
    }

    private void renderStoragePercentageCircle(List<StorageDomain> storageDomainList) {
        long availableStorageSizeMb = 0;
        long usedStorageSizeMb = 0;
        for (StorageDomain storageDomain : storageDomainList) {
            availableStorageSizeMb += storageDomain.getAvailableSizeMb();
            usedStorageSizeMb += storageDomain.getUsedSizeMb();
        }
        storagePercentageCircle.setMaxPercentageValue((availableStorageSizeMb + usedStorageSizeMb) / 1024);
        storagePercentageCircle.setPercentageValue(usedStorageSizeMb / 1024);
        storagePercentageCircle.setSummary(getString(R.string.gb_used));
        summaryStoragePercentageCircle.setText(getString(
                R.string.summary_storage_percentage_circle, availableStorageSizeMb / 1024, (availableStorageSizeMb + usedStorageSizeMb) / 1024));
    }
}
