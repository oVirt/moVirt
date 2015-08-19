package org.ovirt.mobile.movirt.ui;

import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.CaCert;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.dialogs.ErrorDialogFragment;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class that represents base Activity for entire moVirt app. Every Activity should extends this if
 * you want to see basic ActionBar options, etc
 * Created by Nika on 25.06.2015.
 */

@EActivity
@OptionsMenu(R.menu.movirt)
public abstract class MovirtActivity extends ActionBarLoaderActivity implements HasProgressBar {
    private static final int CONNECTION_INFO_LOADER = 0;
    protected int numSuperLoaders = 1;
    @Bean
    protected ProviderFacade superProvider;
    @Bean
    protected SyncUtils syncUtils;
    @Bean
    protected MovirtAuthenticator authenticator;
    private LoaderManager loaderManager;
    private ConnectionInfo connectionInfo;
    private ConnectionInfoLoader connectionInfoLoader;
    private ProgressBar progress;

    @Override
    public LoaderManager getSupportLoaderManager() {
        return this.loaderManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = super.getSupportLoaderManager();
        connectionInfoLoader = new ConnectionInfoLoader();
        connectionInfo = new ConnectionInfo();
        loaderManager.initLoader(CONNECTION_INFO_LOADER, null, connectionInfoLoader);
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
        MenuItem connection = menu.findItem(R.id.menu_connection);
        connection.setVisible(connectionInfo.getState() == ConnectionInfo.State.FAILED);
        return super.onPrepareOptionsMenu(menu);
    }

    @OptionsItem(R.id.menu_connection)
    public void onConnectionInfo() {
        Toast.makeText(this, connectionInfo.getMessage(this), Toast.LENGTH_LONG).show();
    }

    @OptionsItem(R.id.action_refresh)
    @Background
    public void onRefresh() {
        syncUtils.triggerRefresh();
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

    @UiThread
    @Override
    public void showProgressBar() {
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    @UiThread
    @Override
    public void hideProgressBar() {
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
    }

    @Receiver(actions = Broadcasts.CONNECTION_FAILURE,
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void connectionFailure(
            @Receiver.Extra(Broadcasts.Extras.CONNECTION_FAILURE_REASON) String reason) {
        String sessionID = AccountManager.get(this).peekAuthToken(
                MovirtAuthenticator.MOVIRT_ACCOUNT,
                MovirtAuthenticator.AUTH_TOKEN_TYPE);
        if (sessionID == null) {
            sessionID = "sessionID is missing!\n\tauthentication failed";
        }
        String apiUrl = "";
        StringBuilder certificate = new StringBuilder();
        if (authenticator.getApiUrl() != null) {
            apiUrl = authenticator.getApiUrl();
            URL url = null;
            try {
                url = new URL(apiUrl);
                if (url.getProtocol().equalsIgnoreCase("https")) {
                    certificate.append("\nCertificate strategy: ")
                            .append(authenticator.getCertHandlingStrategy().toString());
                }
                if (authenticator.getCertHandlingStrategy() == CertHandlingStrategy.TRUST_CUSTOM) {
                    if (superProvider.query(CaCert.class).all().size() > 0) {
                        certificate.append("\n\tcertificate is downloaded and stored");
                    } else {
                        certificate.append("\n\tcertificate is missing!");
                    }
                }
            } catch (MalformedURLException e) {
                apiUrl = "url is malformed\n\t" + e.getMessage();
            }
        } else {
            apiUrl = "url is missing!";
        }
        String message = getString(R.string.rest_req_failed) +
                "\nConnection details:\nAPI URL: " + apiUrl +
                "\nUsername: " + authenticator.getUserName() +
                "\nSessionID: " + sessionID +
                certificate.toString() +
                "\n\nError details:\n" + reason;
        DialogFragment dialogFragment = ErrorDialogFragment.newInstance(message);
        dialogFragment.show(getFragmentManager(), "error");
    }

    @UiThread
    @Receiver(actions = Broadcasts.IN_SYNC, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void syncingChanged(@Receiver.Extra(Broadcasts.Extras.SYNCING) boolean syncing) {
        if (syncing && progress != null) {
            showProgressBar();
        } else {
            hideProgressBar();
        }
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
                connectionInfo.initFromCursor(data);
                invalidateOptionsMenu();
            }
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        }
    }
}
