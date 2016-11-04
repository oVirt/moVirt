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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;

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
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.dialogs.ApiPathDialogFragment;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiver;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.SSLHandshakeException;

@EActivity(R.layout.activity_authenticator)
public class AuthenticatorActivity extends AccountAuthenticatorActivity implements CreateDialogBroadcastReceiver {

    private static final String TAG = AuthenticatorActivity.class.getSimpleName();
    private static final int SECONDS_IN_MINUTE = 60;
    private static final String[] URL_COMPLETE = {"http://", "https://", "ovirt-engine/api", "80",
            "443", "api"};
    private static final String[] USERNAME_COMPLETE = {"admin@", "internal", "admin@internal"};
    private static int REQUEST_ACCOUNT_DETAILS = 1;
    private static volatile boolean inProgress;

    public static final String SHOW_ADVANCED_AUTHENTICATOR = "SHOW_ADVANCED_AUTHENTICATOR";

    @Bean
    NullHostnameVerifier verifier;
    @SystemService
    AccountManager accountManager;
    @Bean
    LoginClient loginClient;
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
    @ViewById
    Button btnCreate;
    @ViewById
    Button btnAdvanced;
    @Bean
    MovirtAuthenticator authenticator;
    @Bean
    SyncUtils syncUtils;
    @Bean
    EventsHandler eventsHandler;
    @Bean
    ProviderFacade providerFacade;
    @InstanceState
    CertHandlingStrategy certHandlingStrategy;
    @InstanceState
    boolean advancedFieldsInited = false;
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
    @Bean
    MessageHelper messageHelper;

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
        loginProgress(inProgress);
        if (getIntent().getBooleanExtra(SHOW_ADVANCED_AUTHENTICATOR, false)) {
            btnAdvancedClicked();
        }
    }

    @AfterInject
    void initAdvanced() {
        if (advancedFieldsInited) {
            return;
        }
        advancedFieldsInited = true;

        certHandlingStrategy = authenticator.getCertHandlingStrategy();
    }

    @Click(R.id.btnAdvanced)
    public void btnAdvancedClicked() {
        Intent intent = new Intent(this, AdvancedAuthenticatorActivity_.class);
        intent.putExtra(AdvancedAuthenticatorActivity.CERT_HANDLING_STRATEGY, certHandlingStrategy.id());
        intent.putExtra(AdvancedAuthenticatorActivity.LOAD_CA_FROM, txtEndpoint.getText().toString());
        startActivityForResult(intent, REQUEST_ACCOUNT_DETAILS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCOUNT_DETAILS) {
            if (resultCode == RESULT_OK) {
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
            messageHelper.showError(ErrorType.USER, getString(R.string.login_error_invalid_api_url,
                    e.getMessage(), getString(R.string.default_endpoint)));
            return;
        }
        endpoint = endpointUrl.toString();

        username = txtUsername.getText().toString();
        if (!username.matches(".+@.+")) {
            messageHelper.showError(ErrorType.USER, getString(R.string.login_error_invalid_username,
                    getString(R.string.account_username), getString(R.string.default_username)));
            return;
        }

        password = txtPassword.getText().toString();
        if (password.length() == 0) {
            messageHelper.showToast(getString(R.string.login_error_empty_password));
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
            endpointUrlFixed = new URL(endpointUrl, getString(R.string.default_api_path));
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
        boolean usernameChanged = !TextUtils.equals(username, authenticator.getUserName());
        boolean urlChanged = !TextUtils.equals(endpoint, authenticator.getApiUrl());

        try {
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

            setLoginInProgress(true); // disables syncs because setUserData() may trigger sync
            // without option SYNC_EXTRAS_EXPEDITED which may be interrupted by our future sync with option SYNC_EXTRAS_EXPEDITED
            setUserData(MovirtAuthenticator.MOVIRT_ACCOUNT, endpoint, username, password, adminPriv);

            String token = loginClient.login(username, password);
            onLoginResultReceived(token, urlChanged, usernameChanged);
        } catch (HttpClientErrorException e) {
            setLoginInProgress(false);
            switch (e.getStatusCode()) {
                case NOT_FOUND:
                    messageHelper.showError(ErrorType.LOGIN, e, getString(R.string.login_error_bad_address_suffix));
                    break;
                case UNAUTHORIZED:
                    messageHelper.showError(ErrorType.LOGIN, e, getString(R.string.login_error_incorrect_username_password));
                    break;
                default:
                    messageHelper.showError(ErrorType.LOGIN, messageHelper.createMessage(e));
                    break;
            }
        } catch (Exception e) {
            setLoginInProgress(false);
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof SSLHandshakeException) {
                fireCertificateError(cause);
            } else if (cause != null && (cause instanceof ConnectException || cause instanceof UnknownHostException)) {
                messageHelper.showError(ErrorType.LOGIN, e, getString(R.string.login_error_incorrect_ip_port));
            } else if (cause != null && cause instanceof SocketTimeoutException) {
                messageHelper.showError(ErrorType.LOGIN, e, getString(R.string.login_error_timeout));
            } else {
                messageHelper.showError(ErrorType.LOGIN, getString(R.string.login_error, e.getMessage()));
            }
        }
    }


    void onLoginResultReceived(String token, boolean urlChanged, boolean usernameChanged) {
        if (TextUtils.isEmpty(token)) {
            setLoginInProgress(false);
            messageHelper.showError(ErrorType.LOGIN,
                    getString(R.string.login_error_empty_token, getString(R.string.ca_management)));
            return;
        }

        if (urlChanged || usernameChanged) {
            // there is a different set of events and since we are counting only the increments,
            // this ones are not needed anymore
            eventsHandler.deleteEvents();
        }

        if (urlChanged) {
            deleteCaFile();
        }

        accountManager.setAuthToken(MovirtAuthenticator.MOVIRT_ACCOUNT, MovirtAuthenticator.AUTH_TOKEN_TYPE, token);
        setLoginInProgress(false);
        messageHelper.showToast(getString(R.string.login_success));
        syncUtils.triggerRefresh();

        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, MovirtAuthenticator.ACCOUNT_NAME);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, MovirtAuthenticator.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Background
    public void deleteCaFile() {
        File file = new File(Constants.getCaCertPath(this));
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    void setLoginInProgress(boolean loginInProgress) {
        inProgress = loginInProgress;
        ContentResolver.setIsSyncable(MovirtAuthenticator.MOVIRT_ACCOUNT,
                OVirtContract.CONTENT_AUTHORITY, loginInProgress ? 0 : 1);
        Intent intent = new Intent(Broadcasts.IN_USER_LOGIN);
        intent.putExtra(Broadcasts.Extras.MESSAGE, loginInProgress);
        getApplicationContext().sendBroadcast(intent);
    }

    public static boolean isInUserLogin() {
        return inProgress;
    }

    @Receiver(actions = {Broadcasts.IN_USER_LOGIN},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void loginProgress(
            @Receiver.Extra(Broadcasts.Extras.MESSAGE) boolean loginInProgress) {
        if (btnCreate != null) {
            btnCreate.setEnabled(!loginInProgress);
        }

        if (btnAdvanced != null) {
            btnAdvanced.setEnabled(!loginInProgress);
        }

        if (authProgress != null) {
            authProgress.setVisibility(loginInProgress ? View.VISIBLE : View.GONE);
        }
    }

    public void fireCertificateError(Throwable cause) {
        int ignoreIndex = Arrays
                .asList(getResources().getStringArray(R.array.cert_option_keys))
                .indexOf("ignore");
        String certIgnore = getResources()
                .getStringArray(R.array.certificate_handling_strategy)[ignoreIndex];
        String message = getString(R.string.login_error_bad_cert, certIgnore, cause.getMessage());

        Intent intent = new Intent(Broadcasts.REST_CA_FAILURE);
        intent.putExtra(Broadcasts.Extras.ERROR_REASON, message);
        getApplicationContext().sendBroadcast(intent);
    }

    private void setUserData(Account account, String apiUrl, String name, String password, Boolean hasAdminPermissions) {
        accountManager.setUserData(account, MovirtAuthenticator.API_URL, apiUrl);
        accountManager.setUserData(account, MovirtAuthenticator.USER_NAME, name);
        accountManager.setUserData(account, MovirtAuthenticator.HAS_ADMIN_PERMISSIONS, Boolean.toString(hasAdminPermissions));
        accountManager.setUserData(account, MovirtAuthenticator.CERT_HANDLING_STRATEGY, Long.toString(certHandlingStrategy.id()));
        accountManager.setPassword(account, password); //triggers sync in later APIs (Android 6)
    }

    @Receiver(actions = {Broadcasts.ERROR_MESSAGE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showErrorDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason,
            @Receiver.Extra(Broadcasts.Extras.REPEATED_MINOR_ERROR) boolean repeatedMinorError) {
        CreateDialogBroadcastReceiverHelper.showErrorDialog(getFragmentManager(), reason, repeatedMinorError);
    }

    @Receiver(actions = {Broadcasts.REST_CA_FAILURE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showCertificateDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason) {
        CreateDialogBroadcastReceiverHelper.showCertificateDialog(getFragmentManager(), reason, false);
    }
}
