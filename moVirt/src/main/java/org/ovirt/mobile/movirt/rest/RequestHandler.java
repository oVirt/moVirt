package org.ovirt.mobile.movirt.rest;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.util.DestroyableListeners;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.ovirt.mobile.movirt.rest.RestHelper.prepareAuthToken;

@EBean
public class RequestHandler implements AccountEnvironment.EnvDisposable{

    private Version version;

    private AccountPropertiesManager propertiesManager;

    private DestroyableListeners listeners;

    private ErrorHandler errorHandler;

    @RootContext
    Context context;

    @Bean
    ConnectivityHelper connectivityHelper;

    public RequestHandler withDefaultErrorHandler(ErrorHandler errorHandler) {
        ObjectUtils.requireNotNull(errorHandler, "errorHandler");

        this.errorHandler = errorHandler;

        return this;
    }

    public RequestHandler init(AccountPropertiesManager propertiesManager) {
        ObjectUtils.requireNotNull(propertiesManager, "propertiesManager");

        this.propertiesManager = propertiesManager;

        listeners = new DestroyableListeners(propertiesManager)
                .notifyAndRegisterListener(new AccountProperty.VersionListener() {
                    @Override
                    public void onPropertyChange(Version newVersion) {
                        version = newVersion;
                    }
                });

        return this;
    }

    @Override
    public void dispose() {
        listeners.destroy();
    }

    /**
     * Uses default handler to handler errors if set
     */
    public <T> void fireRestRequestSafe(final Request<T> request, final Response<T> response) {
        try {
            fireRestRequest(request, response, errorHandler);
        } catch (RestCallException ignore) {
        }
    }

    public <T> void fireRestRequest(final Request<T> request, final Response<T> response) throws RestCallException {
        fireRestRequest(request, response, null);
    }

    public <T> void fireRestRequest(final Request<T> request, Response<T> response, ErrorHandler activeHandler) throws RestCallException {
        if (response == null) {
            response = SimpleResponse.dummyResponse();
        }
        RestCallException error = null;

        response.before();

        try {
            doFireRequestWithPersistentAuth(request, response);
        } catch (RestCallException e) {
            if (e.getCallResult() == RestCallError.AUTH_FAILED) {
                try {
                    // if it is an expired session it has been cleared - try again.
                    // If the credentials were filled well, now it will pass
                    doFireRequestWithPersistentAuth(request, response);
                } catch (RestCallException x) {
                    error = x;
                }
            } else {
                error = e;
            }
        }

        if (error != null) {
            response.onError();

            if (activeHandler == null) {
                response.after();
                throw error;
            } else {
                activeHandler.handleError(error);
            }
        } else { // reset errors in any case on both handlers
            if (errorHandler != null) {
                errorHandler.resetErrors();
            }

            if (activeHandler != null && errorHandler != activeHandler) {
                activeHandler.resetErrors();
            }
        }

        response.after();
    }

    private <T, U extends RestClientRootUrl & RestClientHeaders & RestClientSupport> void doFireRequestWithPersistentAuth(Request<T> request, Response<T> response) throws RestCallException {
        U restClient = request.getRestClient();

        try {
            Bundle bundle = propertiesManager.getAuthToken().getResult();
            if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                if (TextUtils.isEmpty(authToken)) {
                    throw new RestCallException(RestCallError.NO_TOKEN_AVAILABLE);
                } else {
                    prepareAuthToken(restClient, version, authToken);
                    if (!connectivityHelper.isNetworkAvailable()) {
                        throw new RestCallException(RestCallError.NO_NETWORK_AVAILABLE);
                    }
                    T restResponse = request.fire();
                    response.onResponse(restResponse);
                }
            } else {
                throw new RestCallException(RestCallError.NO_CONNECTION_SPECIFIED);
            }
        } catch (ResourceAccessException | AuthenticatorException e) {
            throw new RestCallException(RestCallError.NO_CONNECTION, e);
        } catch (HttpClientErrorException e) {
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                    // ok, session id is not valid anymore - invalidate it
                    propertiesManager.setAuthToken(null);
                    throw new RestCallException(RestCallError.AUTH_FAILED);
                case NOT_FOUND:
                    throw new RestCallException(RestCallError.WRONG_API_URL, restClient.getRootUrl(), e);
                default:
                    throw new RestCallException(RestCallError.HTTP_OTHER_ERROR, e);
            }
        } catch (RestCallException e) {
            throw e;
        } catch (Exception e) {
            throw new RestCallException(RestCallError.OTHER_ERROR, e);
        }
    }

    public interface ErrorHandler {
        void handleError(Throwable exception);

        void resetErrors();
    }
}
