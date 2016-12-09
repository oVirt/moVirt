package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.rest.OvirtTimeoutSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.client.errorhandler.LoginRedirectException;
import org.ovirt.mobile.movirt.rest.dto.Api;
import org.springframework.util.StringUtils;

import java.net.SocketTimeoutException;

import static org.ovirt.mobile.movirt.rest.RestHelper.JSESSIONID;
import static org.ovirt.mobile.movirt.rest.RestHelper.clearAuth;
import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptEncodingHeaderAndFactory;
import static org.ovirt.mobile.movirt.rest.RestHelper.setPersistentV3AuthHeaders;

/**
 * Created by suomiy on 10/14/16.
 */

@EBean(scope = EBean.Scope.Singleton)
public class LoginClient {
    private static final String TAG = LoginClient.class.getSimpleName();

    @RestService
    OVirtLoginV3RestClient loginV3RestClient;

    @RestService
    OVirtLoginV4RestClient loginV4RestClient;

    @Bean
    AccountPropertiesManager accountPropertiesManager;

    @Bean
    OvirtTimeoutSimpleClientHttpRequestFactory timeoutRequestFactory;

    @AfterInject
    public void init() {
        setAcceptEncodingHeaderAndFactory(loginV3RestClient, timeoutRequestFactory);
        setAcceptEncodingHeaderAndFactory(loginV4RestClient, timeoutRequestFactory);

        accountPropertiesManager.notifyAndRegisterListener(new AccountProperty.ApiUrlListener() {
            @Override
            public void onPropertyChange(String apiUrl) {
                loginV3RestClient.setRootUrl(apiUrl);
            }
        });
        accountPropertiesManager.notifyAndRegisterListener(new AccountProperty.ApiBaseUrlListener() {
            @Override
            public void onPropertyChange(String apiBaseUrl) {
                loginV4RestClient.setRootUrl(apiBaseUrl);
            }
        });
    }

    /**
     * @param username username
     * @param password password
     * @return auth token depending on API version
     */
    public String login(String username, String password) {
        String token = "";

        synchronized (loginV4RestClient) {
            try {
                token = loginV4RestClient.login(username, password).getAccessToken();
            } catch (LoginRedirectException e) { // inform about redirect
                throw e;
            } catch (Exception e) {// 405 Method Not Allowed - old API
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
