package org.ovirt.mobile.movirt.ui.storage;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.StorageDomainFacade;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.enums.StorageDomainStatus;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

@EFragment(R.layout.fragment_storage_domain_detail_general)
public class StorageDomainDetailGeneralFragment extends RefreshableLoaderFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = StorageDomainDetailGeneralFragment.class.getSimpleName();

    private String storageDomainId = null;

    @ViewById
    TextView statusView;

    @ViewById
    TextView domainTypeView;

    @ViewById
    TextView storageTypeView;

    @ViewById
    TextView formatView;

    @ViewById
    TextView freeSpaceView;

    @ViewById
    TextView totalSpaceView;

    @ViewById
    SwipeRefreshLayout swipeGeneralContainer;

    @StringRes(R.string.details_for_storage_domain)
    String STORAGE_DOMAIN_DETAILS;

    @Bean
    ProviderFacade provider;

    @Bean
    StorageDomainFacade storageDomainFacade;

    @AfterViews
    void initLoader() {
        Uri storageDomainUri = getActivity().getIntent().getData();
        storageDomainId = storageDomainUri.getLastPathSegment();

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void destroyLoader() {
        getLoaderManager().destroyLoader(0);
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeGeneralContainer;
    }

    @Override
    @Background
    public void onRefresh() {
        storageDomainFacade.syncOne(new ProgressBarResponse<StorageDomain>(this), storageDomainId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return provider.query(StorageDomain.class).id(storageDomainId).asLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToNext()) {
            Log.e(TAG, "Error loading Storage Domain");
            return;
        }
        renderStorageDomain(storageDomainFacade.mapFromCursor(data));
    }

    private void renderStorageDomain(StorageDomain storageDomain) {
        getActivity().setTitle(String.format(STORAGE_DOMAIN_DETAILS, storageDomain.getName()));
        StorageDomainStatus status = storageDomain.getStatus();
        statusView.setText(status != null ? status.toString().toLowerCase() : StorageDomainStatus.UNKNOWN.toString().toLowerCase());
        if (storageDomain.getType() != null) {
            domainTypeView.setText(storageDomain.getType().toString());
        } else {
            domainTypeView.setText(getString(R.string.NA));
        }
        if (storageDomain.getStorageType() != null) {
            storageTypeView.setText(storageDomain.getStorageType().toString());
        } else {
            storageTypeView.setText(getString(R.string.NA));
        }
        formatView.setText(storageDomain.getStorageFormat());

        long usedSize = storageDomain.getUsedSize();
        long availableSize = storageDomain.getAvailableSize();
        if ((usedSize != -1 && availableSize != -1) && (usedSize != 0 || availableSize != 0)) {
            freeSpaceView.setText(new MemorySize(availableSize).toString());
            totalSpaceView.setText(new MemorySize(availableSize + usedSize).toString());
        } else {
            freeSpaceView.setText(getString(R.string.NA));
            totalSpaceView.setText(getString(R.string.NA));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
