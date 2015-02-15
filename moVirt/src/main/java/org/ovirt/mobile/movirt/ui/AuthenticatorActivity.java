package org.ovirt.mobile.movirt.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.mqtt.MqttService_;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.sync.OVirtClient;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.sync.SyncUtils;

@EActivity(R.layout.authenticator_activity)
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

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
    CheckBox chkDisableHttps;

    @ViewById
    CheckBox enforceHttpBasicAuth;

    @ViewById
    CheckBox chkUseDoctorRest;

    @ViewById
    EditText txtDoctorRestUrl;

    @ViewById
    EditText txtDoctorMqttUrl;

    @ViewById
    ProgressBar authProgress;

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    SyncUtils syncUtils;

    @Bean
    EventsHandler eventsHandler;

    @AfterViews
    void init() {
        txtEndpoint.setText(authenticator.getApiUrl());
        txtUsername.setText(authenticator.getUserName());
        txtPassword.setText(authenticator.getPassword());

        chkAdminPriv.setChecked(authenticator.hasAdminPermissions());
        chkDisableHttps.setChecked(authenticator.disableHttps());
        enforceHttpBasicAuth.setChecked(authenticator.enforceBasicAuth());

        chkUseDoctorRest.setChecked(authenticator.useDoctorRest());
        txtDoctorRestUrl.setText(authenticator.getDoctorRestUrl());
        txtDoctorMqttUrl.setText(authenticator.getDoctorMqttUrl());
        useDoctorChanged(chkUseDoctorRest.isChecked());
    }

    @Click(R.id.btnCreate)
    void addNew() {
        String endpoint = txtEndpoint.getText().toString();
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        Boolean adminPriv = chkAdminPriv.isChecked();
        Boolean disableHttps = chkDisableHttps.isChecked();
        Boolean enforceHttpBasic = enforceHttpBasicAuth.isChecked();

        Boolean useDoctorRest = chkUseDoctorRest.isChecked();
        String doctorRestUrl = txtDoctorRestUrl.getText().toString();
        String doctorMqttUrl = txtDoctorMqttUrl.getText().toString();

        finishLogin(endpoint, username, password, adminPriv, disableHttps, enforceHttpBasic, useDoctorRest, doctorRestUrl, doctorMqttUrl);
    }

    @Background
    void finishLogin(String apiUrl, String name, String password, Boolean hasAdminPermissions, Boolean disableHttps, Boolean enforceHttpBasic,
                     Boolean useDoctorRest, String doctorRestUrl, String doctorMqttUrl) {
        boolean endpointChanged = false;
        if (!TextUtils.equals(apiUrl, authenticator.getApiUrl()) ||
                !TextUtils.equals(name, authenticator.getUserName())) {
            endpointChanged = true;
        }

        if (accountManager.getAccountsByType(MovirtAuthenticator.ACCOUNT_TYPE).length == 0) {
            accountManager.addAccountExplicitly(MovirtAuthenticator.MOVIRT_ACCOUNT, password, null);
        }

        ContentResolver.setSyncAutomatically(MovirtAuthenticator.MOVIRT_ACCOUNT, OVirtContract.CONTENT_AUTHORITY, true);
        ContentResolver.setIsSyncable(MovirtAuthenticator.MOVIRT_ACCOUNT, OVirtContract.CONTENT_AUTHORITY, 1);

        setUserData(MovirtAuthenticator.MOVIRT_ACCOUNT, apiUrl, name, password, hasAdminPermissions, disableHttps, enforceHttpBasic, useDoctorRest, doctorRestUrl, doctorMqttUrl);

        changeProgressVisibilityTo(View.VISIBLE);
        String token = "";
        boolean success = true;
        try {
            token = client.login(apiUrl, name, password, disableHttps, hasAdminPermissions);
            if (useDoctorRest) {
                MqttService_.intent(getApplication()).start();
            }
        } catch (Exception e) {
            showToast("Error logging in: " + e.getMessage());
            success = false;
            return;
        } finally {
            changeProgressVisibilityTo(View.GONE);
            if (success) {
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
            } else {
                return;
            }
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

    @CheckedChange(R.id.chkUseDoctorRest)
    void useDoctorChanged(boolean checked) {
        txtDoctorRestUrl.setVisibility(checked ? View.VISIBLE : View.GONE);
        txtDoctorMqttUrl.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @UiThread
    void changeProgressVisibilityTo(int visibility) {
        authProgress.setVisibility(visibility);
    }

    @UiThread
    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void setUserData(Account account, String apiUrl, String name, String password,
                             Boolean hasAdminPermissions, Boolean disableHttps, Boolean enforceHttpBasic, Boolean useDoctorRest,
                             String doctorRestUrl, String doctorMqttUrl) {
        accountManager.setUserData(account, MovirtAuthenticator.API_URL, apiUrl);
        accountManager.setUserData(account, MovirtAuthenticator.USER_NAME, name);
        accountManager.setUserData(account, MovirtAuthenticator.HAS_ADMIN_PERMISSIONS, Boolean.toString(hasAdminPermissions));
        accountManager.setUserData(account, MovirtAuthenticator.DISABLE_HTTPS, Boolean.toString(disableHttps));
        accountManager.setUserData(account, MovirtAuthenticator.ENFORCE_HTTP_BASIC, Boolean.toString(enforceHttpBasic));
        accountManager.setUserData(account, MovirtAuthenticator.USE_DOCTOR_REST, Boolean.toString(useDoctorRest));
        accountManager.setUserData(account, MovirtAuthenticator.DOCTOR_REST_URL, doctorRestUrl);
        accountManager.setUserData(account, MovirtAuthenticator.DOCTOR_MQTT_URL, doctorMqttUrl);
        accountManager.setPassword(account, password);
    }

    @Receiver(actions = {Broadcasts.CONNECTION_FAILURE}, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    void connectionFailure(@Receiver.Extra(Broadcasts.Extras.CONNECTION_FAILURE_REASON) String reason) {
        Toast.makeText(AuthenticatorActivity.this, R.string.rest_req_failed + " " + reason, Toast.LENGTH_LONG).show();
    }
}
