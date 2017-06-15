package org.ovirt.mobile.movirt.ui.storage;

import android.content.Intent;
import android.database.Cursor;
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
import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.enums.StorageDomainStatus;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.ovirt.mobile.movirt.ui.RefreshableLoaderFragment;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

@EFragment(R.layout.fragment_storage_domain_detail_general)
public class StorageDomainDetailGeneralFragment extends RefreshableLoaderFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = StorageDomainDetailGeneralFragment.class.getSimpleName();

    private String storageDomainId = null;
    private MovirtAccount account;

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

    @Bean
    ProviderFacade provider;

    @Bean
    EnvironmentStore environmentStore;

    @AfterViews
    void initLoader() {
        final Intent intent = getActivity().getIntent();
        storageDomainId = intent.getData().getLastPathSegment();
        account = intent.getParcelableExtra(Constants.ACCOUNT_KEY);

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
        environmentStore.safeEntityFacadeCall(account, StorageDomain.class,
                facade -> facade.syncOne(new ProgressBarResponse<>(this), storageDomainId));
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
        renderStorageDomain(EntityMapper.forEntity(StorageDomain.class).fromCursor(data));
    }

    private void renderStorageDomain(StorageDomain storageDomain) {
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
