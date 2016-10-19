package org.ovirt.mobile.movirt.rest;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.rest.dto.ErrorBody;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collection;

import static org.ovirt.mobile.movirt.rest.RestHelper.JSESSIONID;
import static org.ovirt.mobile.movirt.rest.RestHelper.resetClientSettings;
import static org.ovirt.mobile.movirt.rest.RestHelper.setPersistentV3AuthHeaders;
import static org.ovirt.mobile.movirt.rest.RestHelper.updateClientBeforeCall;

@EBean(scope = EBean.Scope.Singleton)
public class RequestHandler {
    private static final String TAG = RequestHandler.class.getSimpleName();

    private ObjectMapper mapper = new ObjectMapper();

    @Bean
    ProviderFacade provider;

    @RootContext
    Context context;

    @SystemService
    AccountManager accountManager;

    @Bean
    LoginClient loginClient;

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    NotificationHelper notificationHelper;

    @Bean
    SharedPreferencesHelper sharedPreferencesHelper;

    @StringRes(R.string.rest_request_failed)
    String errorMsg;

    private boolean isV3Api = true;

    @AfterInject
    public void init() {
        isV3Api = authenticator.getApiVersion().isV3Api();
        loginClient.registerListener(new LoginClient.ApiVersionChangedListener() {
            @Override
            public void onVersionChanged(org.ovirt.mobile.movirt.auth.Version version) {
                isV3Api = version.isV3Api();
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

        RestCallResult result = doFireRequestWithPersistentAuth(request, response, request.getRestClient()); // not possible to create generic variable inside
        if (result == RestCallResult.AUTH_ERROR) {
            // if it is an expired session it has been cleared - try again.
            // If the credentials were filled well, now it will pass
            result = doFireRequestWithPersistentAuth(request, response, request.getRestClient());
        }

        if (result != RestCallResult.SUCCESS && response != null) {
            response.onError();
        }

        if (response != null) {
            response.after();
        }

        if (result != RestCallResult.CONNECTION_ERROR) {
            updateConnectionInfo(true);
        }
    }

    private <T, U extends RestClientRootUrl & RestClientHeaders & RestClientSupport> RestCallResult doFireRequestWithPersistentAuth(Request<T> request, Response<T> response, U restClient) {
        AccountManagerFuture<Bundle> resp = accountManager.getAuthToken(MovirtAuthenticator.MOVIRT_ACCOUNT, MovirtAuthenticator.AUTH_TOKEN_TYPE, null, false, null, null);
        boolean success = false;

        try {
            Bundle result = resp.getResult();
            if (result.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);

                if (TextUtils.isEmpty(authToken)) {
                    fireOtherConnectionError("Empty auth token");
                } else {
                    resetClientSettings(restClient);
                    updateClientBeforeCall(restClient, authenticator);
                    if (isV3Api) {
                        restClient.setCookie(JSESSIONID, authToken);
                        setPersistentV3AuthHeaders(restClient);
                    } else {
                        restClient.setBearerAuth(authToken);
                    }

                    try {
                        T restResponse = request.fire();
                        success = true;
                        if (response != null) {
                            response.onResponse(restResponse);
                        }
                        return RestCallResult.SUCCESS;

                    } catch (NestedRuntimeException e) {
                        HttpStatus statusCode = null;

                        if (e instanceof ResourceAccessException) {
                            fireConnectionErrorAndUpdateInfo(e);
                            return RestCallResult.CONNECTION_ERROR;
                        }

                        if (e instanceof HttpClientErrorException) {
                            statusCode = ((HttpClientErrorException) e).getStatusCode();
                        }

                        if (statusCode == HttpStatus.UNAUTHORIZED) {
                            // ok, session id is not valid anymore - invalidate it
                            accountManager.invalidateAuthToken(MovirtAuthenticator.AUTH_TOKEN_TYPE, authToken);
                            accountManager.setAuthToken(MovirtAuthenticator.MOVIRT_ACCOUNT, MovirtAuthenticator.AUTH_TOKEN_TYPE, null);
                            return RestCallResult.AUTH_ERROR;
                        } else {
                            fireOtherConnectionError(e, restClient.getRootUrl());
                            return RestCallResult.OTHER_ERROR;
                        }
                    }

                }
            } else if (result.containsKey(AccountManager.KEY_INTENT)) {
                Intent accountAuthenticatorResponse = result.getParcelable(AccountManager.KEY_INTENT);
                Intent editConnectionIntent = new Intent(Broadcasts.NO_CONNECTION_SPEFICIED);
                editConnectionIntent.putExtra(AccountManager.KEY_INTENT, accountAuthenticatorResponse);
                context.sendBroadcast(editConnectionIntent);

                return RestCallResult.OTHER_ERROR;
            }
        } catch (Exception e) {
            if (e instanceof ResourceAccessException || e instanceof AuthenticatorException) {
                fireConnectionErrorAndUpdateInfo(e);
                return RestCallResult.CONNECTION_ERROR;
            }
            fireOtherConnectionError(e, restClient.getRootUrl());
        }

        if (!success) {
            return RestCallResult.OTHER_ERROR;
        } else {
            return RestCallResult.SUCCESS;
        }

    }

    private ConnectionInfo updateConnectionInfo(boolean success) {
        ConnectionInfo connectionInfo;
        ConnectionInfo.State state;
        boolean prevFailed = false;
        boolean configured = sharedPreferencesHelper.isConnectionNotificationEnabled();
        Collection<ConnectionInfo> connectionInfos = provider.query(ConnectionInfo.class).all();
        int size = connectionInfos.size();

        if (size != 0) {
            connectionInfo = connectionInfos.iterator().next();
            ConnectionInfo.State lastState = connectionInfo.getState();
            if (lastState == ConnectionInfo.State.FAILED || lastState == ConnectionInfo.State.FAILED_REPEATEDLY) {
                prevFailed = true;
            }
        } else {
            connectionInfo = new ConnectionInfo();
        }

        if (!success) {
            state = prevFailed ? ConnectionInfo.State.FAILED_REPEATEDLY : ConnectionInfo.State.FAILED;
        } else {
            state = ConnectionInfo.State.OK;
        }
        connectionInfo.updateWithCurrentTime(state);

        //update in DB
        if (size != 0) {
            provider.batch().update(connectionInfo).apply();
        } else {
            provider.batch().insert(connectionInfo).apply();
        }

        //show Notification
        if (!success && !prevFailed && configured) {
            Intent resultIntent = new Intent(context, MainActivity_.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            0
                    );
            notificationHelper.showConnectionNotification(
                    context, resultPendingIntent, connectionInfo);
        }

        return connectionInfo;
    }

    private void fireOtherConnectionError(Exception e, String rootUrl) {
        String msg = e.getMessage();
        if (e instanceof HttpClientErrorException) {
            HttpStatus statusCode = ((HttpClientErrorException) e).getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                msg = msg + ": " + "oVirt-engine is not found on " + rootUrl;
                fireOtherConnectionError(msg);
                return;
            }

            String responseBody = ((HttpClientErrorException) e).getResponseBodyAsString();
            if (!TextUtils.isEmpty(responseBody)) {
                msg = msg + ": " + responseBody;
                try {
                    ErrorBody errorBody = mapper.readValue(((HttpClientErrorException) e).getResponseBodyAsByteArray(), ErrorBody.class);
                    if (errorBody.fault != null) {
                        msg = e.getMessage() + " " + errorBody.fault.reason + " " + errorBody.fault.detail;
                    } else {
                        try {
                            ErrorBody.Fault fault = mapper.readValue(((HttpClientErrorException) e).getResponseBodyAsByteArray(), ErrorBody.Fault.class);
                            if (fault != null) {
                                msg = e.getMessage() + " " + fault.reason + " " + fault.detail;
                            }
                        } catch (Exception exception) {
                            // msg inited to proper response body already
                        }
                    }


                } catch (Exception e1) {
                    // msg inited to proper response body already
                }

            }
        }

        fireOtherConnectionError(msg);
    }

    private void fireConnectionErrorAndUpdateInfo(Exception e) {
        ConnectionInfo connectionInfo = updateConnectionInfo(false);
        Intent intent = getConnectionFailiureIntent(e.getMessage());
        boolean failedRepeatedly = connectionInfo.getState() == ConnectionInfo.State.FAILED_REPEATEDLY;
        intent.putExtra(Broadcasts.Extras.REPEATED_CONNECTION_FAILURE, failedRepeatedly);
        context.sendBroadcast(intent);
    }

    private void fireOtherConnectionError(String msg) {
        final Intent intent = getConnectionFailiureIntent(msg);
        context.sendBroadcast(intent);
    }

    private Intent getConnectionFailiureIntent(String msg) {
        Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
        intent.putExtra(Broadcasts.Extras.FAILURE_REASON, String.format(errorMsg, msg));
        return intent;
    }

    private enum RestCallResult {
        SUCCESS,
        AUTH_ERROR,
        CONNECTION_ERROR,
        OTHER_ERROR
    }

}
