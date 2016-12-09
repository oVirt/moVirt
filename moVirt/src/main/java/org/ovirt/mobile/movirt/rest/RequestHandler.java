package org.ovirt.mobile.movirt.rest;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.property.Version;
import org.ovirt.mobile.movirt.util.message.ErrorType;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.ovirt.mobile.movirt.rest.RestHelper.prepareAuthToken;

@EBean(scope = EBean.Scope.Singleton)
public class RequestHandler {
    private static final String TAG = RequestHandler.class.getSimpleName();
    @RootContext
    Context context;

    @Bean
    AccountPropertiesManager accountPropertiesManager;

    @Bean
    MessageHelper messageHelper;

    private Version version;

    @AfterInject
    public void init() {
        accountPropertiesManager.notifyAndRegisterListener(new AccountProperty.VersionListener() {
            @Override
            public void onPropertyChange(Version newVersion) {
                version = newVersion;
            }
        });
    }

    /**
     * has to be synced because of error handling - otherwise it would not be possible to bind the error
     */
    public synchronized <T> void fireRestRequest(final Request<T> request, final Response<T> response) {
        if (response != null) {
            response.before();
        }

        RestCallResult result = doFireRequestWithPersistentAuth(request, response);
        if (result == RestCallResult.AUTH_ERROR) {
            // if it is an expired session it has been cleared - try again.
            // If the credentials were filled well, now it will pass
            result = doFireRequestWithPersistentAuth(request, response);
        }

        if (result != RestCallResult.SUCCESS && response != null) {
            response.onError();
        }

        if (response != null) {
            response.after();
        }
    }

    private <T, U extends RestClientRootUrl & RestClientHeaders & RestClientSupport> RestCallResult doFireRequestWithPersistentAuth(Request<T> request, Response<T> response) {
        RestCallResult result = RestCallResult.OTHER_ERROR;
        U restClient = request.getRestClient();

        try {
            Bundle bundle = accountPropertiesManager.getAuthToken().getResult();
            if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                if (TextUtils.isEmpty(authToken)) {
                    messageHelper.showError(ErrorType.REST_MAJOR, context.getString(R.string.rest_token_missing));
                } else {
                    prepareAuthToken(restClient, version, authToken);
                    T restResponse = request.fire();
                    result = RestCallResult.SUCCESS;
                    if (response != null) {
                        response.onResponse(restResponse);
                    }
                }
            } else if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                Intent accountAuthenticatorResponse = bundle.getParcelable(AccountManager.KEY_INTENT);
                Intent editConnectionIntent = new Intent(Broadcasts.NO_CONNECTION_SPECIFIED);
                editConnectionIntent.putExtra(AccountManager.KEY_INTENT, accountAuthenticatorResponse);
                context.sendBroadcast(editConnectionIntent);

                result = RestCallResult.CONNECTION_ERROR;
            }
        } catch (ResourceAccessException | AuthenticatorException e) {
            messageHelper.showError(ErrorType.REST_MINOR, e);
            result = RestCallResult.CONNECTION_ERROR;
        } catch (HttpClientErrorException e) {
            String msg = e.getMessage();
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                    // ok, session id is not valid anymore - invalidate it
                    accountPropertiesManager.setAuthToken(null);
                    result = RestCallResult.AUTH_ERROR;
                    break;
                case NOT_FOUND:
                    messageHelper.showError(ErrorType.REST_MAJOR,
                            context.getString(R.string.rest_url_missing, msg, restClient.getRootUrl()));
                    break;
                default:
                    messageHelper.showError(ErrorType.REST_MAJOR, messageHelper.createMessage(e));
                    break;
            }
        } catch (Exception e) {
            messageHelper.showError(ErrorType.REST_MAJOR, e);
        }

        if (result == RestCallResult.SUCCESS) {
            messageHelper.resetMinorErrors();
        }

        return result;
    }

    private enum RestCallResult {
        SUCCESS,
        AUTH_ERROR,
        CONNECTION_ERROR,
        OTHER_ERROR
    }
}
