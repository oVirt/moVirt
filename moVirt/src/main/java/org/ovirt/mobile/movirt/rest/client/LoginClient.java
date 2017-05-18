package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.rest.client.errorhandler.LoginRedirectException;
import org.ovirt.mobile.movirt.rest.client.requestfactory.OvirtSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.dto.Api;
import org.ovirt.mobile.movirt.util.DestroyableListeners;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.net.SocketTimeoutException;

import static org.ovirt.mobile.movirt.rest.RestHelper.JSESSIONID;
import static org.ovirt.mobile.movirt.rest.RestHelper.clearAuth;
import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptEncodingHeaderAndFactory;
import static org.ovirt.mobile.movirt.rest.RestHelper.setPersistentV3AuthHeaders;

@EBean
public class LoginClient implements AccountEnvironment.EnvDisposable {

    private AccountPropertiesManager accountPropertiesManager;

    private DestroyableListeners listeners;

    @RestService
    OVirtLoginV3RestClient loginV3RestClient;

    @RestService
    OVirtLoginV4RestClient loginV4RestClient;

    public LoginClient init(AccountPropertiesManager accountPropertiesManager, OvirtSimpleClientHttpRequestFactory timeoutRequestFactory) {
        ObjectUtils.requireNotNull(accountPropertiesManager, "accountPropertiesManager");
        ObjectUtils.requireNotNull(timeoutRequestFactory, "timeoutRequestFactory");
        this.accountPropertiesManager = accountPropertiesManager;

        setAcceptEncodingHeaderAndFactory(loginV3RestClient, timeoutRequestFactory);
        setAcceptEncodingHeaderAndFactory(loginV4RestClient, timeoutRequestFactory);

        listeners = new DestroyableListeners(accountPropertiesManager)
                .notifyAndRegisterListener(new AccountProperty.ApiUrlListener() {
                    @Override
                    public void onPropertyChange(String apiUrl) {
                        loginV3RestClient.setRootUrl(apiUrl);
                    }
                }).notifyAndRegisterListener(new AccountProperty.ApiBaseUrlListener() {
                    @Override
                    public void onPropertyChange(String apiBaseUrl) {
                        loginV4RestClient.setRootUrl(apiBaseUrl);
                    }
                });

        return this;
    }

    @Override
    public void dispose() {
        listeners.destroy();
    }

    /**
     * @param username username
     * @param password password
     * @return auth token depending on API version
     */
    public String login(String username, String password) throws AccountDeletedException {
        String token = "";

        synchronized (loginV4RestClient) {
            try {
                token = loginV4RestClient.login(username, password).getAccessToken();
            } catch (LoginRedirectException e) { // inform about redirect
                throw e;
            } catch (Exception e) { // 405 Method Not Allowed - old API
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof SocketTimeoutException) {
                    throw e;
                }
            }
        }

        boolean oldApi = StringUtils.isEmpty(token);

        synchronized (loginV3RestClient) {
            clearAuth(loginV3RestClient);
            if (oldApi) {
                loginV3RestClient.setHttpBasicAuth(username, password);
                setPersistentV3AuthHeaders(loginV3RestClient);
            } else {
                loginV3RestClient.setBearerAuth(token);
            }

            Api api = loginV3RestClient.login();
            accountPropertiesManager.setApiVersion(api);

            if (oldApi && api != null) { // check for api because v4 may set JSESSIONID even if login was unsuccessful
                token = loginV3RestClient.getCookie(JSESSIONID);
            }
        }

        return token;
    }
}
