package org.ovirt.mobile.movirt.ui;

import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.AccountManagerHelper;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.AllAccounts;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.account.data.SyncStatus;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.ConnectivityHelper;
import org.ovirt.mobile.movirt.ui.dialogs.ErrorDialogFragment;
import org.ovirt.mobile.movirt.util.Disposables;
import org.ovirt.mobile.movirt.util.message.CommonMessageHelper;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EActivity
@OptionsMenu(R.menu.movirt)
public abstract class SyncableActivity extends ActionBarLoaderActivity implements HasProgressBar {

    private List<ConnectionInfo> failedInfos = Collections.emptyList();
    private Disposables disposables = new Disposables();

    private LoaderManager loaderManager;
    private ConnectionInfoLoader connectionInfoLoader;
    private ProgressBar progress;
    private static final int CONNECTION_INFO_LOADER = 0;

    public static final int FIRST_CHILD_LOADER = 1;

    @Bean
    protected ProviderFacade providerFacade;

    @Bean
    protected AccountManagerHelper accountManagerHelper;

    @Bean
    protected AccountRxStore rxStore;

    @Bean
    protected ConnectivityHelper connectivityHelper;

    @Bean
    protected CommonMessageHelper commonMessageHelper;

    @OptionsMenuItem(R.id.menu_connection)
    protected MenuItem connectionInfoTriangle;

    @Override
    public LoaderManager getSupportLoaderManager() {
        return this.loaderManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = super.getSupportLoaderManager();
        connectionInfoLoader = new ConnectionInfoLoader();
        loaderManager.initLoader(CONNECTION_INFO_LOADER, null, connectionInfoLoader);
    }

    @AfterViews
    protected void afterViewsInit() {
        disposables.add(rxStore.ACTIVE_SELECTION.distinctUntilChanged()
                .switchMap(activeSelection -> rxStore.isSyncInProgressObservable(activeSelection.getAccount()))
                .onErrorReturnItem(new SyncStatus(false))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(syncStatus -> {
                    if (syncStatus.isInProgress()) {
                        showProgressBar();
                    } else {
                        hideProgressBar();
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        disposables.destroy();
        super.onDestroy();
    }

    @Override
    public void restartLoader() {
        loaderManager.restartLoader(CONNECTION_INFO_LOADER, null, connectionInfoLoader);
    }

    @Override
    public void destroyLoader() {
        loaderManager.destroyLoader(CONNECTION_INFO_LOADER);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        connectionInfoTriangle.setVisible(!failedInfos.isEmpty());
        if (!failedInfos.isEmpty()) {
            connectionInfoTriangle.setTitle(String.format(Locale.ENGLISH, " %d/%d", failedInfos.size(), rxStore.getAllAccounts().size()));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.menu_connection)
    public void onConnectionInfo() {
        StringBuilder sb = new StringBuilder();
        AllAccounts allAccounts = rxStore.getAllAccountsWrapped();

        if (failedInfos.size() > 1) {
            sb.append("Scroll to see errors across all accounts.\n\n\n");
        }
        for (ConnectionInfo info : failedInfos) {
            final MovirtAccount account = allAccounts.getAccountById(info.getAccountId());
            sb.append("Account ").append(account == null ? "_REMOVED_ACCOUNT_" : account.getName())
                    .append("\n\n")
                    .append(info.getMessage(this))
                    .append("\n\n\n");
        }

        DialogFragment dialogFragment = ErrorDialogFragment.newInstance(sb.toString());
        dialogFragment.show(getFragmentManager(), "connection_info");
    }

    @OptionsItem(R.id.action_refresh)
    @Background
    public void onRefresh() {
        if (!connectivityHelper.isNetworkAvailable()) {
            // show toast but allow system to notice refresh
            commonMessageHelper.showToast(getString(R.string.rest_no_network));
        }

        final ActiveSelection activeSelection = rxStore.getActiveSelection();

        if (activeSelection.isAllAccounts()) {
            for (MovirtAccount account : rxStore.getAllAccounts()) {
                accountManagerHelper.triggerRefresh(account);
            }
        } else {
            accountManagerHelper.triggerRefresh(activeSelection.getAccount());
        }
    }

    /**
     * Method to set progress bar defined by child class and handled by parent class
     *
     * @param progress ProgressBar defined in child layout
     */
    public void setProgressBar(ProgressBar progress) {
        this.progress = progress;
        hideProgressBar();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressBar() {
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void hideProgressBar() {
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
    }

    private class ConnectionInfoLoader implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return providerFacade
                    .query(ConnectionInfo.class)
                    .whereIn(OVirtContract.ConnectionInfo.STATE, new String[]{
                            ConnectionInfo.State.FAILED.name(),
                            ConnectionInfo.State.FAILED_REPEATEDLY.name()
                    })
                    .asLoader();
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
            if (cursor != null) {
                failedInfos = EntityMapper.forEntity(ConnectionInfo.class).listFromCursor(cursor);
                invalidateOptionsMenu();
            }
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        }
    }
}
