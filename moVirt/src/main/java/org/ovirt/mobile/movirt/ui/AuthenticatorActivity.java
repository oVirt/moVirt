package org.ovirt.mobile.movirt.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.NullHostnameVerifier;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.sync.SyncUtils;

@EActivity(R.layout.authenticator_activity)
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private static final String TAG = AuthenticatorActivity.class.getSimpleName();
    private static final int SECONDS_IN_MINUTE = 60;
    private static int REQUEST_ACCOUNT_DETAILS = 1;
    @Bean
    NullHostnameVerifier verifier;
    @SystemService
    AccountManager accountManager;
    @Bean
    OVirtClient client;
    @ViewById
    EditText txtEndpoint;
    @ViewById
    EditText txtUsername;
    @ViewById
    EditText txtPassword;
    @ViewById
    CheckBox chkAdminPriv;
    @ViewById
    ProgressBar authProgress;
    @Bean
    MovirtAuthenticator authenticator;
    @Bean
    SyncUtils syncUtils;
    @Bean
    EventsHandler eventsHandler;
    @Bean
    ProviderFacade providerFacade;
    @InstanceState
    boolean enforceHttpBasicAuth = false;

    @InstanceState
    CertHandlingStrategy certHandlingStrategy;

    @InstanceState
    boolean advancedFieldsInited = false;

    @InstanceState
    boolean inProgress;

    @AfterViews
    void init() {
        txtEndpoint.setText(authenticator.getApiUrl());
        txtUsername.setText(authenticator.getUserName());
        txtPassword.setText(authenticator.getPassword());
        chkAdminPriv.setChecked(authenticator.hasAdminPermissions());
        changeProgressVisibilityTo(inProgress ? View.VISIBLE : View.GONE);
    }

    @AfterInject
    void initAdvanced() {
        if (advancedFieldsInited) {
            return;
        }
        advancedFieldsInited = true;

        enforceHttpBasicAuth = authenticator.enforceBasicAuth();
        certHandlingStrategy = authenticator.getCertHandlingStrategy();
    }

    @Click(R.id.btnAdvanced)
    void btnAdvancedClicked() {
        Intent intent = new Intent(this, AdvancedAuthenticatorActivity_.class);
        intent.putExtra(AdvancedAuthenticatorActivity.ENFORCE_HTTP_BASIC_AUTH, enforceHttpBasicAuth);
        intent.putExtra(AdvancedAuthenticatorActivity.CERT_HANDLING_STRATEGY, certHandlingStrategy.id());
        intent.putExtra(AdvancedAuthenticatorActivity.LOAD_CA_FROM, txtEndpoint.getText().toString());
        startActivityForResult(intent, REQUEST_ACCOUNT_DETAILS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCOUNT_DETAILS) {
            if (resultCode == RESULT_OK) {
                enforceHttpBasicAuth = data.getBooleanExtra(AdvancedAuthenticatorActivity.ENFORCE_HTTP_BASIC_AUTH, enforceHttpBasicAuth);
                long certHandlingStrategyId = data.getLongExtra(AdvancedAuthenticatorActivity.CERT_HANDLING_STRATEGY, certHandlingStrategy.id());
                certHandlingStrategy = CertHandlingStrategy.from(certHandlingStrategyId);
            }
        }
    }

    @Click(R.id.btnCreate)
    void addNew() {
        String endpoint = txtEndpoint.getText().toString();
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        Boolean adminPriv = chkAdminPriv.isChecked();

        finishLogin(endpoint, username, password, adminPriv);
    }

    @Background
    void finishLogin(String apiUrl, String name, String password, Boolean hasAdminPermissions) {
        boolean endpointChanged = false;
        if (!TextUtils.equals(apiUrl, authenticator.getApiUrl()) ||
                !TextUtils.equals(name, authenticator.getUserName())) {
            endpointChanged = true;
        }

        if (accountManager.getAccountsByType(MovirtAuthenticator.ACCOUNT_TYPE).length == 0) {
            if (accountManager.addAccountExplicitly(MovirtAuthenticator.MOVIRT_ACCOUNT, password, null)) {
                ContentResolver.setIsSyncable(MovirtAuthenticator.MOVIRT_ACCOUNT, OVirtContract.CONTENT_AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(MovirtAuthenticator.MOVIRT_ACCOUNT, OVirtContract.CONTENT_AUTHORITY, true);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                if (sharedPreferences.getBoolean(SettingsActivity.KEY_PERIODIC_SYNC, false)) {
                    int intervalInMinutes = SettingsActivity.getSyncIntervalInMinutes(sharedPreferences);
                    addPeriodicSync(intervalInMinutes);
                }
            }
        }

        setUserData(MovirtAuthenticator.MOVIRT_ACCOUNT, apiUrl, name, password, hasAdminPermissions);

        changeProgressVisibilityTo(View.VISIBLE);

        try {
            String token = client.login(apiUrl, name, password, hasAdminPermissions);
            onTokenReceived(token, endpointChanged);
        } catch (Exception e) {
            changeProgressVisibilityTo(View.GONE);
            showToast("Error logging in: " + e.getMessage());
        }
    }

    public static void addPeriodicSync(int intervalInMinutes) {
        long intervalInSeconds =
                (long) intervalInMinutes * (long) SECONDS_IN_MINUTE;
        ContentResolver.addPeriodicSync(
                MovirtAuthenticator.MOVIRT_ACCOUNT,
                OVirtContract.CONTENT_AUTHORITY,
                Bundle.EMPTY,
                intervalInSeconds);
    }

    void onTokenReceived(String token, boolean endpointChanged) {
        changeProgressVisibilityTo(View.GONE);
        if (TextUtils.isEmpty(token)) {
            showToast("Error: the returned token is empty");
            return;
        } else {
            showToast("Login successful");
            if (endpointChanged) {
                // there is a different set of events and since we are counting only the increments, this ones are not needed anymore
                eventsHandler.deleteEvents();
            }

            syncUtils.triggerRefresh();
        }
        accountManager.setAuthToken(MovirtAuthenticator.MOVIRT_ACCOUNT, MovirtAuthenticator.AUTH_TOKEN_TYPE, token);

        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, MovirtAuthenticator.ACCOUNT_NAME);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, MovirtAuthenticator.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @UiThread
    void changeProgressVisibilityTo(int visibility) {
        if (visibility == View.GONE) {
            inProgress = true;
        } else {
            inProgress = true;
        }

        authProgress.setVisibility(visibility);
    }

    @UiThread
    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void setUserData(Account account, String apiUrl, String name, String password, Boolean hasAdminPermissions) {
        accountManager.setUserData(account, MovirtAuthenticator.API_URL, apiUrl);
        accountManager.setUserData(account, MovirtAuthenticator.USER_NAME, name);
        accountManager.setUserData(account, MovirtAuthenticator.HAS_ADMIN_PERMISSIONS, Boolean.toString(hasAdminPermissions));
        accountManager.setUserData(account, MovirtAuthenticator.CERT_HANDLING_STRATEGY, Long.toString(certHandlingStrategy.id()));
        accountManager.setUserData(account, MovirtAuthenticator.ENFORCE_HTTP_BASIC, Boolean.toString(enforceHttpBasicAuth));
        accountManager.getUserData(account, MovirtAuthenticator.API_URL);
        accountManager.setPassword(account, password);
    }

    @Receiver(actions = {Broadcasts.CONNECTION_FAILURE}, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void connectionFailure(@Receiver.Extra(Broadcasts.Extras.CONNECTION_FAILURE_REASON) String reason) {
        Toast.makeText(AuthenticatorActivity.this, R.string.rest_req_failed + " " + reason, Toast.LENGTH_LONG).show();
    }
}
