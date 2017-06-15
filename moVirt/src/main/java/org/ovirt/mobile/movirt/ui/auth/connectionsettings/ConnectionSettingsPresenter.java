package org.ovirt.mobile.movirt.ui.auth.connectionsettings;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.AccountManagerHelper;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.account.data.ActiveSelection;
import org.ovirt.mobile.movirt.auth.account.data.LoginStatus;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.SmallMistakeException;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.WrongApiPathException;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.WrongArgumentException;
import org.ovirt.mobile.movirt.ui.mvp.AccountDisposablesPresenter;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@EBean
public class ConnectionSettingsPresenter extends AccountDisposablesPresenter<ConnectionSettingsPresenter, ConnectionSettingsContract.View>
        implements ConnectionSettingsContract.Presenter {

    private MessageHelper messageHelper;

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @Bean
    EnvironmentStore environmentStore;

    @Bean
    Resources resources;

    @Bean
    AccountManagerHelper accountManagerHelper;

    @Override
    public ConnectionSettingsPresenter initialize() {
        super.initialize();

        try {
            messageHelper = environmentStore.getMessageHelper(account);
            AccountPropertiesManager propertiesManager = environmentStore.getAccountPropertiesManager(account);

            getView().setPasswordVisibility(commonSharedPreferencesHelper.isPasswordVisible());
            getView().displayApiUrl(propertiesManager.getApiUrl());
            getView().displayUserName(propertiesManager.getUsername());
            getView().displayPassword(propertiesManager.getPassword());
            getView().displayTitle(account.getName());

            getView().showLoginInProgress(environmentStore.isLoginInProgress(account));
            getDisposables().add(rxStore.LOGIN_STATUS
                    .filter(loginStatus -> loginStatus.isAccount(account))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(loginStatus -> getView().showLoginInProgress(loginStatus.isInProgress())));
        } catch (AccountDeletedException e) {
            finishSafe();
        }
        return this;
    }

    @Override
    public void togglePasswordVisibility() {
        final boolean toggledVisibility = !commonSharedPreferencesHelper.isPasswordVisible();
        commonSharedPreferencesHelper.setPasswordVisibility(toggledVisibility);
        getView().setPasswordVisibility(toggledVisibility);
    }

    @Override
    public void onCertificateManagementClicked(String apiUrl) {
        getView().startCertificateManagementActivity(account, apiUrl);
    }

    @Override
    public void sanitizeAndCheckLoginInfo(LoginInfo loginInfo) throws WrongArgumentException, SmallMistakeException, WrongApiPathException {
        if (loginInfo == null || loginInfo.endpoint == null || loginInfo.username == null || loginInfo.password == null) {
            throw new WrongArgumentException(account, resources.getLoginEmptyFieldsError());
        }

        URL endpointUrl;
        try {
            endpointUrl = new URL(loginInfo.endpoint);
            loginInfo.endpoint = endpointUrl.toString();
        } catch (Exception e) {
            throw new WrongArgumentException(account, resources.getLoginMalformedUrlError(e.getMessage()));
        }

        if (!loginInfo.username.matches(".+@.+")) {
            throw new WrongArgumentException(account, resources.getLoginInvalidUsernameError());
        }

        if (loginInfo.password.length() == 0) {
            throw new SmallMistakeException(resources.getLoginEmptyPasswordError());
        }

        if (loginInfo.password.contains("+")) {
            messageHelper.showToast(null, resources.getLoginPlusPasswordError(VersionSupport.PLUS_SIGN_IN_PASSWORD.getSupportedFrom()));
        }

        String apiPath = endpointUrl.getPath();
        if (apiPath.isEmpty() || !apiPath.contains("api")) {
            throw new WrongApiPathException(account, loginInfo);
        }
    }

    @Background
    @Override
    public void login(LoginInfo loginInfo) {
        try {
            setLoginProgress(true);
            setUserData(loginInfo);
            String token = environmentStore.getLoginClient(account).login(loginInfo.username, loginInfo.password);
            onLoginResultReceived(token);
        } catch (AccountDeletedException e) {
            setLoginProgress(false);
            messageHelper.showToast(resources.getLoginAccountDeletedError());
        } catch (HttpClientErrorException e) {
            setLoginProgress(false);
            HttpStatus statusCode = e.getStatusCode();

            switch (statusCode.series()) {
                case REDIRECTION:
                    messageHelper.showError(ErrorType.USER, e.getMessage());
                    break;
                default:
                    switch (statusCode) {
                        case NOT_FOUND:
                            messageHelper.showError(ErrorType.LOGIN, e, resources.getLoginBadAdressSuffixError());
                            break;
                        case UNAUTHORIZED:
                            messageHelper.showError(ErrorType.LOGIN, e, resources.getLoginIncorrectCredentialsError());
                            break;
                        default:
                            messageHelper.showError(ErrorType.LOGIN, messageHelper.createMessage(e));
                            break;
                    }
            }
        } catch (Exception e) {
            setLoginProgress(false);
            Throwable cause = e.getCause();
            if (cause == null) {
                messageHelper.showError(ErrorType.LOGIN, resources.getLoginError(e.getMessage()));
            } else if (cause instanceof SSLHandshakeException) {
                resources.showCertificateError(account, resources.getLoginCertificateError(cause), loginInfo.endpoint);
            } else if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
                messageHelper.showError(ErrorType.LOGIN, e, resources.getLoginIncorrectIpPortError());
            } else if (cause instanceof SocketTimeoutException) {
                messageHelper.showError(ErrorType.LOGIN, e, resources.getLoginTimeoutError());
            }
        }
    }

    private void setLoginProgress(boolean loginProgress) {
        rxStore.LOGIN_STATUS.onNext(new LoginStatus(account, loginProgress));
    }

    private void onLoginResultReceived(String token) throws AccountDeletedException {
        if (StringUtils.isEmpty(token)) {
            setLoginProgress(false);
            messageHelper.showError(ErrorType.LOGIN, resources.getLoginEmptyTokenError());
            return;
        }
        final AccountPropertiesManager propertiesManager = environmentStore.getAccountPropertiesManager(account);

        if (propertiesManager.isFirstLogin()) {
            // there is a different set of events, so the old ones are not needed anymore
            environmentStore.getEventProviderHelper(account).deleteEvents();
            propertiesManager.setFirstLogin(false);
        }

        propertiesManager.setAuthToken(token);
        setLoginProgress(false);

        // sync
        if (accountManagerHelper.isSyncable(account)) {
            accountManagerHelper.triggerRefresh(account);
        } else { // account has never been initialized
            accountManagerHelper.setAccountSyncable(account, true);
            if (!accountManagerHelper.isPeriodicSyncable(account)) {
                accountManagerHelper.triggerRefresh(account);
            } // otherwise periodic sync will trigger immediate refresh
        }

        rxStore.ACTIVE_SELECTION.onNext(new ActiveSelection(account));
        messageHelper.showToast(resources.getLoginSuccess());
        finishSafe();
    }

    private void setUserData(LoginInfo loginInfo) throws AccountDeletedException {
        AccountPropertiesManager propertiesManager = environmentStore.getAccountPropertiesManager(account);

        boolean usernameChanged = propertiesManager.propertyDiffers(AccountProperty.USERNAME, loginInfo.username);
        boolean urlChanged = propertiesManager.propertyDiffers(AccountProperty.API_URL, loginInfo.endpoint);

        if (urlChanged || usernameChanged) { // there can be more attempts to login so set it only the first time
            propertiesManager.setFirstLogin(true);
        }

        propertiesManager.setApiUrl(loginInfo.endpoint);
        propertiesManager.setUsername(loginInfo.username);
        propertiesManager.setAdminPermissions(loginInfo.adminPrivileges);
        propertiesManager.setPassword(loginInfo.password);
    }
}
