package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;
import org.ovirt.mobile.movirt.auth.*;
import org.ovirt.mobile.movirt.auth.Version;
import org.ovirt.mobile.movirt.rest.OvirtTimeoutSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.dto.Api;
import org.springframework.util.StringUtils;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static org.ovirt.mobile.movirt.rest.RequestHelper.JSESSIONID;
import static org.ovirt.mobile.movirt.rest.RequestHelper.initClient;
import static org.ovirt.mobile.movirt.rest.RequestHelper.resetClientSettings;
import static org.ovirt.mobile.movirt.rest.RequestHelper.setPersistentV3AuthHeaders;
import static org.ovirt.mobile.movirt.rest.RequestHelper.updateClientBeforeCall;

/**
 * Created by suomiy on 10/14/16.
 */

@EBean(scope = EBean.Scope.Singleton)
public class LoginClient {
    private static final String TAG = LoginClient.class.getSimpleName();

    private List<ApiVersionChangedListener> listeners = new ArrayList<>();

    @RestService
    OVirtLoginV3RestClient loginV3RestClient;

    @RestService
    OVirtLoginV4RestClient loginV4RestClient;

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    OvirtTimeoutSimpleClientHttpRequestFactory timeoutRequestFactory;

    @AfterInject
    public void init() {
        initClient(loginV3RestClient, timeoutRequestFactory);
        initClient(loginV4RestClient, timeoutRequestFactory);
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
                updateClientBeforeCall(loginV4RestClient, authenticator.getBaseUrl(), authenticator);
                token = loginV4RestClient.login(username, password).getAccessToken();
            } catch (Exception ex) {// 405 Method Not Allowed - old API
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof SocketTimeoutException) {
                    throw ex;
                }
            }
        }

        boolean oldApi = StringUtils.isEmpty(token);

        synchronized (loginV3RestClient) {
            resetClientSettings(loginV3RestClient);
            updateClientBeforeCall(loginV3RestClient, authenticator);
            if (oldApi) {
                loginV3RestClient.setHttpBasicAuth(username, password);
                setPersistentV3AuthHeaders(loginV3RestClient);
            } else {
                loginV3RestClient.setBearerAuth(token);
            }

            Api api = loginV3RestClient.login();
            notifyVersionChanged(api);

            if (oldApi && api != null) { // check for api because v4 may set JSESSIONID even if login was unsuccessful
                token = loginV3RestClient.getCookie(JSESSIONID);
            }
        }

        return token;
    }

    public void registerListener(ApiVersionChangedListener listener) {
        listeners.add(listener);
    }

    private void notifyVersionChanged(Api api) {
        org.ovirt.mobile.movirt.auth.Version version = api.toVersion();
        if (!authenticator.getApiVersion().equals(version)) {
            authenticator.setApiVersion(version);
            for (ApiVersionChangedListener listener : listeners) {
                listener.onVersionChanged(version);
            }
        }
    }

    public interface ApiVersionChangedListener {
        void onVersionChanged(Version version);
    }
}
