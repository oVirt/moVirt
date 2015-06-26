package org.ovirt.mobile.movirt.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.sync.SyncUtils;

/**
 * Class that represents base Activity for entire moVirt app. Every Activity should extends this if
 * you want to see basic ActionBar options, etc
 * Created by Nika on 25.06.2015.
 */

@EActivity
@OptionsMenu(R.menu.movirt)
public abstract class MoVirtActivity extends ActionBarActivity {
    private static final int CONNECTION_INFO_LOADER = 0;
    protected int numSuperLoaders = 1;
    @Bean
    protected ProviderFacade superProvider;
    @Bean
    protected SyncUtils syncUtils;
    private LoaderManager loaderManager;
    private boolean connectionIconVisibility = false;
    private String connectionState = "unknown";
    private String connectionAttempt = "unknown";
    private String connectionSuccess = "unknown";
    private ConnectionInfoLoader connectionInfoLoader;

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem connection = menu.findItem(R.id.menu_connection);
        connection.setVisible(connectionIconVisibility);
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.menu_connection)
    public void onConnectionInfo() {
        Toast.makeText(this, "Connection: " + connectionState +
                ".\nLast attempt: " + connectionAttempt +
                ".\nLast successful: " + connectionSuccess, Toast.LENGTH_LONG).show();
    }

    @OptionsItem(R.id.action_refresh)
    @Background
    public void onRefresh() {
        syncUtils.triggerRefresh();
    }

    private class ConnectionInfoLoader implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return superProvider.query(ConnectionInfo.class).asLoader();
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
            if (data != null && data.getCount() > 0) {
                data.moveToFirst();
                int stateIndex = data.getColumnIndex(ConnectionInfo.STATE);
                int attemptIndex = data.getColumnIndex(ConnectionInfo.ATTEMPT);
                int successIndex = data.getColumnIndex(ConnectionInfo.SUCCESSFUL);

                connectionState = data.getString(stateIndex);
                ConnectionInfo.State state = ConnectionInfo.State.valueOf(connectionState);
                connectionIconVisibility = (state == ConnectionInfo.State.FAILED);
                connectionAttempt = data.getString(attemptIndex);
                String success = data.getString(successIndex);
                if (success != null) {
                    connectionSuccess = data.getString(successIndex);
                }

                invalidateOptionsMenu();
            }
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        }
    }
}
