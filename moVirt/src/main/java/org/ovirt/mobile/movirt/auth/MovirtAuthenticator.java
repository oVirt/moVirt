package org.ovirt.mobile.movirt.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.rest.Api;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;


@EBean
public class MovirtAuthenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_NAME = "oVirt";

    public static final String ACCOUNT_TYPE = "org.ovirt.mobile.movirt.authenticator";

    public static final String AUTH_TOKEN_TYPE = "org.ovirt.mobile.movirt.token";

    public static final String USER_NAME = "org.ovirt.mobile.movirt.username";

    public static final String API_URL = "org.ovirt.mobile.movirt.apiurl";

    public static final String API_MAJOR_VERSION = "org.ovirt.mobile.movirt.apimajorversion";

    public static final String API_MINOR_VERSION = "org.ovirt.mobile.movirt.apiminorversion";

    public static final String API_BUILD_VERSION = "org.ovirt.mobile.movirt.apibuildversion";

    public static final String CERT_HANDLING_STRATEGY = "org.ovirt.mobile.movirt.certhandlingstrategy";

    public static final String HAS_ADMIN_PERMISSIONS = "org.ovirt.mobile.movirt.adminpermissionsm";

    public static final String CUSTOM_CERTIFICATE = "org.ovirt.mobile.movirt.customCertificate";

    public static final Account MOVIRT_ACCOUNT = new Account(MovirtAuthenticator.ACCOUNT_NAME, MovirtAuthenticator.ACCOUNT_TYPE);


    private static final String API_FALLBACK_MAJOR_VERSION = "3";
    private static final String API_FALLBACK_MINOR_VERSION = "0";
    private static final String API_FALLBACK_BUILD_VERSION = "0";

    @Bean
    OVirtClient client;

    @SystemService
    AccountManager accountManager;

    private Context context;

    public MovirtAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s2, String[] strings, Bundle options) throws NetworkErrorException {
        if (accountConfigured()) {
            showToast("Only one moVirt account is allowed per device");
            return null;
        } else {
            return createAccountActivity(accountAuthenticatorResponse);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            final String username = getUserName();
            final String password = getPassword();
            if (username != null && password != null) {
                try {
                    if (!AuthenticatorActivity.isInUserLogin()) { // do not attempt to login while user tries
                        authToken = client.login(username, password);
                    }
                } catch (Exception x) { // do not fail on bad login info
                }

                if (!TextUtils.isEmpty(authToken)) {
                    accountManager.setAuthToken(account, authTokenType, authToken);
                }
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }
        return createAccountActivity(accountAuthenticatorResponse);
    }

    private Bundle createAccountActivity(AccountAuthenticatorResponse accountAuthenticatorResponse) {
        final Intent intent = new Intent(context, AuthenticatorActivity_.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

    public CertHandlingStrategy getCertHandlingStrategy() {
        String strategy = read(CERT_HANDLING_STRATEGY);
        if (TextUtils.isEmpty(strategy)) {
            return CertHandlingStrategy.TRUST_SYSTEM;
        }

        try {
            return CertHandlingStrategy.from(Long.valueOf(strategy));
        } catch (NumberFormatException e) {
            return CertHandlingStrategy.TRUST_SYSTEM;
        }
    }

    public String getApiUrl() {
        return read(API_URL);
    }

    public String getBaseUrl() {
        return getApiUrl().replaceFirst("/api$", "");
    }


    public Version getApiVersion() {
        return new Version(getApiMajorVersionAsInt(), getApiMinorVersionAsInt(),
                getApiBuildVersionAsInt());
    }

    public String getApiMajorVersion() {
        return read(API_MAJOR_VERSION, API_FALLBACK_MAJOR_VERSION);
    }

    public String getApiMinorVersion() {
        return read(API_MINOR_VERSION, API_FALLBACK_MINOR_VERSION);
    }

    public String getApiBuildVersion() {
        return read(API_BUILD_VERSION, API_FALLBACK_BUILD_VERSION);
    }

    public String getApiFallbackMajorVersion() {
        return API_FALLBACK_MAJOR_VERSION;
    }

    private int getApiMajorVersionAsInt() {
        return Integer.parseInt(getApiMajorVersion());
    }

    private int getApiMinorVersionAsInt() {
        return Integer.parseInt(getApiMinorVersion());
    }

    private int getApiBuildVersionAsInt() {
        return Integer.parseInt(getApiBuildVersion());
    }

    public boolean isApiWithinRange(Version from, Version to) {
        Version current = getApiVersion();
        return current.compareTo(from) >= 0 && current.compareTo(to) <= 0;
    }

    public boolean isV3Api() {
        return getApiMajorVersionAsInt() < 4;
    }

    public boolean isV4Api() {
        return getApiMajorVersionAsInt() >= 4;
    }

    public void setApiVersion(Api api) {
        try {
            org.ovirt.mobile.movirt.rest.Version version = api.getProductInfo().getVersion();
            setApiMajorVersion(version.getMajor());
            setApiMinorVersion(version.getMinor());
            setApiBuildVersion(version.getBuild());
        } catch (Exception x) {
            setApiMajorVersion(""); // fallback versions are used instead
            setApiMinorVersion("");
            setApiBuildVersion("");
        }
    }

    private void setApiMajorVersion(String majorVersion) {
        accountManager.setUserData(MOVIRT_ACCOUNT, API_MAJOR_VERSION, majorVersion);
    }

    private void setApiMinorVersion(String minorVersion) {
        accountManager.setUserData(MOVIRT_ACCOUNT, API_MINOR_VERSION, minorVersion);
    }

    private void setApiBuildVersion(String buildVersion) {
        accountManager.setUserData(MOVIRT_ACCOUNT, API_BUILD_VERSION, buildVersion);
    }

    public String getUserName() {
        return read(USER_NAME);
    }

    public String getPassword() {
        return accountManager.getPassword(MOVIRT_ACCOUNT);
    }

    public Boolean hasAdminPermissions() {
        return read(HAS_ADMIN_PERMISSIONS, false);
    }

    public boolean accountConfigured() {
        return accountManager.getAccountsByType(ACCOUNT_TYPE).length > 0;
    }

    private Boolean read(String id, boolean defRes) {
        String res = accountManager.getUserData(MOVIRT_ACCOUNT, id);
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return Boolean.valueOf(res);
    }

    private String read(String id, String defRes) {
        String res = accountManager.getUserData(MOVIRT_ACCOUNT, id);
        if (TextUtils.isEmpty(res)) {
            return defRes;
        }

        return res;
    }

    private String read(String id) {
        return accountManager.getUserData(MOVIRT_ACCOUNT, id);
    }

    public static class Version implements Comparable<Version> {
        private int major;
        private int minor;
        private int build;

        public Version(int major, int minor, int build) {
            this.major = major;
            this.minor = minor;
            this.build = build;
        }

        public int getMajor() {
            return major;
        }

        public void setMajor(int major) {
            this.major = major;
        }

        public int getMinor() {
            return minor;
        }

        public void setMinor(int minor) {
            this.minor = minor;
        }

        public int getBuild() {
            return build;
        }

        public void setBuild(int build) {
            this.build = build;
        }

        @Override
        public String toString() {
            return String.format("%s.%s.%s", major, minor, build);
        }

        /**
         * @param another version to be compared to
         * @return a negative integer, zero, or a positive integer if this object version is less than, equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(Version another) {
            if (major == another.major) {
                if (minor == another.minor) {
                    return build - another.build;
                }
                return minor - another.minor;
            }
            return major - another.major;
        }
    }
}
