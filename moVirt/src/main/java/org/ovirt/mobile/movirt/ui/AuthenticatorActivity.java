package org.ovirt.mobile.movirt.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
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
import org.ovirt.mobile.movirt.ui.dialogs.ApiPathDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.ErrorDialogFragment;
import org.ovirt.mobile.movirt.ui.dialogs.ImportCertificateDialogFragment;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.SSLHandshakeException;

@EActivity(R.layout.activity_authenticator)
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private static final String TAG = AuthenticatorActivity.class.getSimpleName();
    private static final int SECONDS_IN_MINUTE = 60;
    private static final String[] URL_COMPLETE = {"http://", "https://", "ovirt-engine/api", "80",
            "443", "api"};
    private static final String[] USERNAME_COMPLETE = {"admin@", "internal", "admin@internal"};
    private static int REQUEST_ACCOUNT_DETAILS = 1;
    @Bean
    NullHostnameVerifier verifier;
    @SystemService
    AccountManager accountManager;
    @Bean
    OVirtClient client;
    @ViewById
    MultiAutoCompleteTextView txtEndpoint;
    @ViewById
    MultiAutoCompleteTextView txtUsername;
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
    @InstanceState
    URL endpointUrl;
    @InstanceState
    String username;
    @InstanceState
    String password;
    @InstanceState
    Boolean adminPriv;
    @InstanceState
    String endpoint;
    @Bean
    SharedPreferencesHelper sharedPreferencesHelper;

    public static void addPeriodicSync(int intervalInMinutes) {
        long intervalInSeconds =
                (long) intervalInMinutes * (long) SECONDS_IN_MINUTE;
        ContentResolver.addPeriodicSync(
                MovirtAuthenticator.MOVIRT_ACCOUNT,
                OVirtContract.CONTENT_AUTHORITY,
                Bundle.EMPTY,
                intervalInSeconds);
    }

    @AfterViews
    void init() {
        txtEndpoint.setText(authenticator.getApiUrl());
        ArrayAdapter<String> urlAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, URL_COMPLETE);
        txtEndpoint.setAdapter(urlAdapter);
        txtEndpoint.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {
            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;
                while (i > 0 && text.charAt(i - 1) != '/' && text.charAt(i - 1) != ':') {
                    i--;
                }
                return i;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();
                while (i < len) {
                    if (text.charAt(i) == '/') {
                        return i;
                    } else {
                        i++;
                    }
                }
                return len;
            }

            @Override
            public CharSequence terminateToken(CharSequence text) {
                return text;
            }
        });

        txtUsername.setText(authenticator.getUserName());
        ArrayAdapter<String> usernameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, USERNAME_COMPLETE);
        txtUsername.setAdapter(usernameAdapter);
        txtUsername.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {
            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;
                while (i > 0 && text.charAt(i - 1) != '@') {
                    i--;
                }
                return i;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();
                while (i < len) {
                    if (text.charAt(i) == '@') {
                        return i;
                    } else {
                        i++;
                    }
                }
                return len;
            }

            @Override
            public CharSequence terminateToken(CharSequence text) {
                return text;
            }
        });

        txtPassword.setText(authenticator.getPassword());
        chkAdminPriv.setChecked(true);
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
    public void btnAdvancedClicked() {
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
        endpoint = txtEndpoint.getText().toString();
        try {
            endpointUrl = new URL(endpoint);
        } catch (MalformedURLException e) {
            String message = "Invalid API URL: " + e.getMessage() + "\nExample: " +
                    getString(R.string.default_endpoint);
            showError(message);
            return;
        }
        endpoint = endpointUrl.toString();

        username = txtUsername.getText().toString();
        if (!username.matches(".+@.+")) {
            showError("Invalid username. Use " +
                    getString(R.string.account_username) + " pattern.\nExample: " +
                    getString(R.string.default_username));
            return;
        }

        password = txtPassword.getText().toString();
        if (password.length() == 0) {
            showToast("Password can't be empty.");
            return;
        }

        adminPriv = chkAdminPriv.isChecked();

        String apiPath = endpointUrl.getPath();
        if (apiPath.isEmpty() || !apiPath.contains("api")) {
            DialogFragment apiPathDialog = new ApiPathDialogFragment();
            apiPathDialog.show(getFragmentManager(), "apiPathDialog");
        } else {
            finishLogin();
        }
    }

    public void fixUrlAndLogin() {
        URL endpointUrlFixed = null;
        try {
            endpointUrlFixed =
                    new URL(endpointUrl, getString(R.string.default_api_path));
        } catch (MalformedURLException ignored) {
        }
        assert endpointUrlFixed != null;
        endpoint = endpointUrlFixed.toString();
        txtEndpoint.setText(endpoint);
        finishLogin();
    }

    @Background
    public void finishLogin() {
        if (endpoint == null || username == null || password == null) {
            return;
        }
        boolean endpointChanged = false;
        if (!TextUtils.equals(endpoint, authenticator.getApiUrl()) ||
                !TextUtils.equals(username, authenticator.getUserName())) {
            endpointChanged = true;
        }

        if (accountManager.getAccountsByType(MovirtAuthenticator.ACCOUNT_TYPE).length == 0) {
            if (accountManager.addAccountExplicitly(MovirtAuthenticator.MOVIRT_ACCOUNT, password, null)) {
                ContentResolver.setIsSyncable(MovirtAuthenticator.MOVIRT_ACCOUNT, OVirtContract.CONTENT_AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(MovirtAuthenticator.MOVIRT_ACCOUNT, OVirtContract.CONTENT_AUTHORITY, true);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                if (sharedPreferences.getBoolean(SharedPreferencesHelper.KEY_PERIODIC_SYNC, false)) {
                    int intervalInMinutes = sharedPreferencesHelper.getSyncIntervalInMinutes();
                    addPeriodicSync(intervalInMinutes);
                }
            }
        }

        setUserData(MovirtAuthenticator.MOVIRT_ACCOUNT, endpoint, username, password, adminPriv);

        changeProgressVisibilityTo(View.VISIBLE);

        try {
            String token = client.login(endpoint, username, password, adminPriv);
            onTokenReceived(token, endpointChanged);
        } catch (Exception e) {
            changeProgressVisibilityTo(View.GONE);
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof SSLHandshakeException) {
                int ignoreIndex = Arrays
                        .asList(getResources().getStringArray(R.array.cert_option_keys))
                        .indexOf("ignore");
                String certIgnore = getResources()
                        .getStringArray(R.array.certificate_handling_strategy)[ignoreIndex];
                String message = "Use proper certificate, or select " + certIgnore +
                        ".\nError details: " + cause.getMessage();
                DialogFragment importCertificateDialog =
                        ImportCertificateDialogFragment.newInstance(message,
                                AdvancedAuthenticatorActivity.MODE_REST_CA_MANAGEMENT,
                                enforceHttpBasicAuth, certHandlingStrategy.id(), endpoint);
                importCertificateDialog.show(getFragmentManager(), "certificateDialog");
            } else {
                showError("Error logging in: " + e.getMessage());
            }
        }
    }

    void onTokenReceived(String token, boolean endpointChanged) {
        changeProgressVisibilityTo(View.GONE);
        if (TextUtils.isEmpty(token)) {
            showError("Error: the returned token is empty." +
                    "\nTry https protocol and add your certificate in " +
                    getString(R.string.advanced_settings) + ".");
            return;
        } else {
            showToast("Login successful");
            if (endpointChanged) {
                // there is a different set of events and since we are counting only the increments,
                // this ones are not needed anymore
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
        inProgress = visibility != View.GONE;
        authProgress.setVisibility(visibility);
    }

    void showError(String msg) {
        DialogFragment dialogFragment = ErrorDialogFragment.newInstance(msg);
        dialogFragment.show(getFragmentManager(), "login_error");
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

    @Receiver(actions = {Broadcasts.CONNECTION_FAILURE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void connectionFailure(
            @Receiver.Extra(Broadcasts.Extras.CONNECTION_FAILURE_REASON) String reason) {
        DialogFragment dialogFragment = ErrorDialogFragment
                .newInstance(this, authenticator, providerFacade, reason);
        dialogFragment.show(getFragmentManager(), "error");
    }
}
