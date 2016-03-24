package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

@EFragment(R.layout.fragment_dashboard_general)
public class DashboardGeneralFragment extends LoaderFragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
    private DecimalFormat decimalFormat = new DecimalFormat("0.#");

    @AfterViews
    void init() {
        getLoaderManager().initLoader(HOST_LOADER, null, this);
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

        if (cpuSpeed < 1000) {
            cpuPercentageCircle.setMaxPercentageValue(cpuSpeed);
            cpuPercentageCircle.setPercentageValue(usedCpuSpeed);
            cpuPercentageCircle.setSummary(getString(R.string.mhz_used));
            summaryCpuPercentageCircle.setText(getString(
                    R.string.summary_cpu_percentage_circle_mhz, cpuSpeed - usedCpuSpeed, cpuSpeed));
        } else {
            cpuPercentageCircle.setMaxPercentageValue(cpuSpeed / 1000f);
            cpuPercentageCircle.setPercentageValue(usedCpuSpeed / 1000f, decimalFormat);
            cpuPercentageCircle.setSummary(getString(R.string.ghz_used));
            summaryCpuPercentageCircle.setText(getString(R.string.summary_cpu_percentage_circle_ghz,
                    decimalFormat.format((cpuSpeed - usedCpuSpeed) / 1000f), decimalFormat.format(cpuSpeed / 1000f)));
        }

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

    private void renderMemoryPercentageCircle(List<Host> hostList) {
        long memorySizeMb = 0;
        long usedMemorySizeMb = 0;
        for (Host host : hostList) {
            memorySizeMb += host.getMemorySizeMb();
            usedMemorySizeMb += host.getMemorySizeMb() * host.getMemoryUsage() / 100;
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

    private void renderStoragePercentageCircle(List<StorageDomain> storageDomainList) {
        long availableStorageSizeMb = 0;
        long usedStorageSizeMb = 0;
        for (StorageDomain storageDomain : storageDomainList) {
            availableStorageSizeMb += storageDomain.getAvailableSizeMb();
            usedStorageSizeMb += storageDomain.getUsedSizeMb();
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
        getLoaderManager().restartLoader(HOST_LOADER, null, this);
        getLoaderManager().restartLoader(STORAGE_DOMAIN_LOADER, null, this);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(HOST_LOADER);
        getLoaderManager().destroyLoader(STORAGE_DOMAIN_LOADER);
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
}
