package org.ovirt.mobile.movirt.ui.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.manager.OnThread;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.sync.SyncUtils;
import org.ovirt.mobile.movirt.ui.UiUtils;
import org.ovirt.mobile.movirt.ui.dialogs.ApiPathDialogFragment;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiver;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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
    private static final String[] URL_COMPLETE = {"http://", "https://", "ovirt-engine/api", "80",
            "443", "api"};
    private static final String[] USERNAME_COMPLETE = {"admin@", "internal", "admin@internal"};
    private static volatile boolean inProgress;

    public static final String SHOW_ADVANCED_AUTHENTICATOR = "SHOW_ADVANCED_AUTHENTICATOR";

    @Bean
    AccountPropertiesManager propertiesManager;
    @Bean
    LoginClient loginClient;
    @ViewById
    MultiAutoCompleteTextView txtEndpoint;
    @ViewById
    MultiAutoCompleteTextView txtUsername;
    @ViewById
    EditText txtPassword;
    @ViewById
    ImageView passwordVisibility;
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
    MessageHelper messageHelper;

    private PropertyChangedListener[] listeners;

    @AfterViews
    void init() {
        if (!propertiesManager.accountConfigured()) {
            if (authenticator.initAccount("")) {
                messageHelper.showToast("Added new account.");
            }
        }

        txtEndpoint.setText(propertiesManager.getApiUrl());
        ArrayAdapter<String> urlAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, URL_COMPLETE);
        txtEndpoint.setAdapter(urlAdapter);
        txtEndpoint.setTokenizer(UiUtils.getUrlTokenizer());

        txtUsername.setText(propertiesManager.getUsername());
        ArrayAdapter<String> usernameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, USERNAME_COMPLETE);
        txtUsername.setAdapter(usernameAdapter);
        txtUsername.setTokenizer(UiUtils.getUsernameTokenizer());

        txtPassword.setText(propertiesManager.getPassword());
        chkAdminPriv.setChecked(true);
        loginProgress(inProgress);
        if (getIntent().getBooleanExtra(SHOW_ADVANCED_AUTHENTICATOR, false)) {
            btnAdvancedClicked();
        }

        initViewListeners();
        initPropertyListeners();
    }

    private void initPropertyListeners() {
        final AccountProperty.PasswordVisibilityListener passVisibilityListener = new AccountProperty.PasswordVisibilityListener() {
            @Override
            public void onPropertyChange(Boolean passwordVisibility) {
                setPasswordVisibility(passwordVisibility);
            }
        };
        listeners = new PropertyChangedListener[]{passVisibilityListener};
        propertiesManager.notifyAndRegisterListener(passVisibilityListener);
    }

    @Override
    protected void onDestroy() {
        for (PropertyChangedListener listener : listeners) {
            propertiesManager.removeListener(listener);
        }
        super.onDestroy();
    }

    @Click(R.id.passwordVisibility)
    void togglePasswordVisibility() {
        propertiesManager.setPasswordVisibility(!propertiesManager.getPasswordVisibility(), OnThread.BACKGROUND);
    }

    private void initViewListeners() {
        passwordVisibility.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        view.setBackgroundColor(UiUtils.addAlphaToColor(Color.WHITE, 0.25f));
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        view.setBackground(null);
                        view.invalidate();
                        break;
                    }
                }

                return false;
            }
        });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setPasswordVisibility(Boolean visible) {
        passwordVisibility.setImageResource(visible ? R.drawable.ic_visibility_white_24dp :
                R.drawable.ic_visibility_off_white_24dp);
        txtPassword.setInputType(visible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Click(R.id.btnAdvanced)
    public void btnAdvancedClicked() {
        Intent intent = new Intent(this, AdvancedAuthenticatorActivity_.class);
        intent.putExtra(AdvancedAuthenticatorActivity.LOAD_CA_FROM, txtEndpoint.getText().toString());
        startActivity(intent);
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

        try {
            setLoginInProgress(true); // disables syncs because setUserData() may trigger sync
            // without option SYNC_EXTRAS_EXPEDITED which may be interrupted by our future sync with option SYNC_EXTRAS_EXPEDITED
            setUserData(endpoint, username, password, adminPriv);

            String token = loginClient.login(username, password);
            onLoginResultReceived(token);
        } catch (HttpClientErrorException e) {
            setLoginInProgress(false);
            HttpStatus statusCode = e.getStatusCode();

            switch (statusCode.series()) {
                case REDIRECTION:
                    messageHelper.showError(ErrorType.USER, e.getMessage());
                    break;
                default:
                    switch (statusCode) {
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

    void onLoginResultReceived(String token) {
        if (TextUtils.isEmpty(token)) {
            setLoginInProgress(false);
            messageHelper.showError(ErrorType.LOGIN,
                    getString(R.string.login_error_empty_token, getString(R.string.certificate_management)));
            return;
        }

        if (propertiesManager.isFirstLogin()) {
            // there is a different set of events and since we are counting only the increments,
            // this ones are not needed anymore
            eventsHandler.deleteEvents();
            propertiesManager.setFirstLogin(false);
        }

        propertiesManager.setAuthToken(token);
        setLoginInProgress(false);
        messageHelper.showToast(getString(R.string.login_success));
        syncUtils.triggerRefresh();

        Account account = authenticator.getAccount();
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);

//        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    void setLoginInProgress(boolean loginInProgress) {
        inProgress = loginInProgress;
        ContentResolver.setIsSyncable(authenticator.getAccount(),
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

    private void setUserData(String apiUrl, String name, String password, Boolean hasAdminPermissions) {
        // mark First Login
        boolean usernameChanged = propertiesManager.propertyDiffers(AccountProperty.USERNAME, username);
        boolean urlChanged = propertiesManager.propertyDiffers(AccountProperty.API_URL, endpoint);

        if (urlChanged || usernameChanged) { // there can be more attempts to login so set it only the first time
            propertiesManager.setFirstLogin(true);
        }

        propertiesManager.setApiUrl(apiUrl);
        propertiesManager.setUsername(name);
        propertiesManager.setAdminPermissions(hasAdminPermissions);
        propertiesManager.setPassword(password); // triggers sync in later APIs (Android 6)
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
