package org.ovirt.mobile.movirt.rest;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.rest.RestService;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.ui.SettingsActivity;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class OVirtClient {
    public static final String JSESSIONID = "JSESSIONID";
    public static final String FILTER = "Filter";
    public static final String PREFER = "Prefer";
    private static final String TAG = OVirtClient.class.getSimpleName();
    ObjectMapper mapper = new ObjectMapper();

    @RestService
    OVirtRestClient restClient;

    @Bean
    OvirtSimpleClientHttpRequestFactory requestFactory;

    @Bean
    ProviderFacade provider;

    @RootContext
    Context context;

    @SystemService
    AccountManager accountManager;

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    NotificationHelper notificationHelper;

    @App
    MoVirtApp app;

    @StringRes(R.string.rest_request_failed)
    String errorMsg;

    private static <E, R extends RestEntityWrapper<E>> List<E> mapRestWrappers(List<R> wrappers, WrapPredicate<R> predicate) {
        List<E> entities = new ArrayList<>();
        if (wrappers == null) {
            return entities;
        }
        for (R rest : wrappers) {
            if (predicate == null || predicate.toWrap(rest)) {
                entities.add(rest.toEntity());
            }
        }
        return entities;
    }

    public void startVm(final String vmId) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.startVm(new Action(), vmId);
                return null;
            }
        }, null);
    }

    public void stopVm(final String vmId) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.stopVm(new Action(), vmId);
                return null;
            }
        }, null);

    }

    public void rebootVm(final String vmId) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.rebootVm(new Action(), vmId);
                return null;
            }
        }, null);
    }

    public void getVm(final String vmId, Response<Vm> response) {
        fireRestRequest(new Request<Vm>() {
            @Override
            public Vm fire() {
                return restClient.getVm(vmId).toEntity();
            }
        }, response);
    }

    public void activateHost(final String hostId) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.activateHost(new Action(), hostId);
                return null;
            }
        }, null);
    }

    public void dectivateHost(final String hostId) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.deactivateHost(new Action(), hostId);
                return null;
            }
        }, null);
    }

    public void getHost(final String hostId, Response<Host> response) {
        fireRestRequest(new Request<Host>() {
            @Override
            public Host fire() {
                return restClient.getHost(hostId).toEntity();
            }
        }, response);
    }

    public void getConsoleTicket(final String vmId, Response<ActionTicket> response) {
        fireRestRequest(new Request<ActionTicket>() {
            @Override
            public ActionTicket fire() {
                return restClient.getConsoleTicket(new Action(), vmId);
            }
        }, response);
    }

    public void getDisks(final String id, Response<Disks> response) {
        fireRestRequest(new Request<Disks>() {
            @Override
            public Disks fire() {
                return restClient.getDisks(id);
            }
        }, response);
    }

    public void getVms(Response<List<Vm>> response) {
        fireRestRequest(new Request<List<Vm>>() {
            @Override
            public List<Vm> fire() {
                Vms loadedVms = null;
                if (authenticator.hasAdminPermissions()) {
                    int maxVms = asIntWithDefault("max_vms_polled", "-1");
                    String query = PreferenceManager.getDefaultSharedPreferences(app).getString("vms_search_query", "");
                    if (!"".equals(query)) {
                        loadedVms = restClient.getVms(query, maxVms);
                    } else {
                        loadedVms = restClient.getVms(maxVms);
                    }

                } else {
                    loadedVms = restClient.getVms(-1);
                }

                if (loadedVms == null) {
                    return new ArrayList<>();
                }

                return mapRestWrappers(loadedVms.vm, null);
            }
        }, response);
    }

    public void getClusters(Response<List<Cluster>> response) {
        fireRestRequest(new Request<List<Cluster>>() {
            @Override
            public List<Cluster> fire() {
                Clusters loadedClusters = restClient.getClusters();
                if (loadedClusters == null) {
                    return new ArrayList<>();
                }

                return mapRestWrappers(loadedClusters.cluster, null);
            }
        }, response);
    }

    public void getDataCenters(Response<List<DataCenter>> response) {
        fireRestRequest(new Request<List<DataCenter>>() {
            @Override
            public List<DataCenter> fire() {
                DataCenters loadedDataCenters = restClient.getDataCenters();
                if (loadedDataCenters == null) {
                    return new ArrayList();
                }

                return mapRestWrappers(loadedDataCenters.data_center, null);
            }
        }, response);
    }

    public void getNics(final String id, Response<Nics> response) {
        fireRestRequest(new Request<Nics>() {
            @Override
            public Nics fire() {
                return restClient.getNics(id);
            }
        }, response);
    }

    public void getHosts(Response<List<Host>> response) {
        fireRestRequest(new Request<List<Host>>() {
            @Override
            public List<Host> fire() {
                Hosts loadedHosts = restClient.getHosts();
                if (loadedHosts == null) {
                    return new ArrayList<>();
                }

                return mapRestWrappers(loadedHosts.host, null);
            }
        }, response);
    }

    public String login(String apiUrl, String username, String password, final boolean hasAdminPrivileges) {
        setPersistentAuthHeaders();
        restClient.setRootUrl(apiUrl);
        restClient.setHttpBasicAuth(username, password);
        restClient.setCookie("JSESSIONID", "");
        requestFactory.setCertificateHandlingMode(authenticator.getCertHandlingStrategy());
        restClient.setHeader(FILTER, Boolean.toString(!hasAdminPrivileges));
        restClient.login();
        String sessionId = restClient.getCookie("JSESSIONID");
        restClient.setHttpBasicAuth("", "");
        return sessionId;
    }

    public void getEventsSince(final int lastEventId, Response<List<Event>> response) {
        fireRestRequest(new Request<List<Event>>() {
            @Override
            public List<Event> fire() {
                Events loadedEvents = null;

                if (authenticator.hasAdminPermissions()) {
                    int maxEventsStored = asIntWithDefault("max_events_stored", EventsHandler.MAX_EVENTS_LOCALLY);

                    String query = PreferenceManager.getDefaultSharedPreferences(app).getString("events_search_query", "");
                    if (!"".equals(query)) {
                        loadedEvents = restClient.getEventsSince(Integer.toString(lastEventId), query, maxEventsStored);
                    } else {
                        loadedEvents = restClient.getEventsSince(Integer.toString(lastEventId), maxEventsStored);
                    }
                } else {
                    loadedEvents = restClient.getEventsSince(Integer.toString(lastEventId), -1);
                }


                if (loadedEvents == null) {
                    return new ArrayList<>();
                }

                return mapRestWrappers(loadedEvents.event, new WrapPredicate<org.ovirt.mobile.movirt.rest.Event>() {
                    @Override
                    public boolean toWrap(org.ovirt.mobile.movirt.rest.Event entity) {
                        return entity.id > lastEventId;
                    }
                });
            }
        }, response);
    }

    @AfterInject
    void initClient() {
        restClient.setHeader("Accept-Encoding", "gzip");

        restClient.getRestTemplate().setRequestFactory(requestFactory);
    }

    /**
     * has to be synced because of error handling - otherwise it would not be possible to bind the error
     */
    private synchronized <T> void fireRestRequest(final Request<T> request, final Response<T> response) {
        if (authenticator.enforceBasicAuth()) {
            fireRequestWithHttpBasicAuth(request, response);
        } else {
            fireRequestWithPersistentAuth(request, response);
        }
    }

    private <T> void fireRequestWithHttpBasicAuth(Request<T> request, Response<T> response) {
        String userName = authenticator.getUserName();
        String password = authenticator.getPassword();

        boolean success = false;
        boolean connectionSuccess = false;

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(authenticator.getApiUrl())) {
            Intent accountAuthenticatorResponse = new Intent(context, AuthenticatorActivity_.class);
            Intent editConnectionIntent = new Intent(Broadcasts.NO_CONNECTION_SPEFICIED);
            editConnectionIntent.putExtra(AccountManager.KEY_INTENT, accountAuthenticatorResponse);
            context.sendBroadcast(editConnectionIntent);
        } else {
            updateClientBeforeCall();
            restClient.setHttpBasicAuth(userName, password);
            restClient.setHeader(PREFER, "");
            restClient.setHeader(JSESSIONID, "");

            if (response != null) {
                response.before();
            }

            try {
                T restResponse = request.fire();
                success = true;

                if (response != null) {
                    response.onResponse(restResponse);
                }
            } catch (Exception e) {
                fireConnectionError(e);
                connectionSuccess = !(e instanceof ResourceAccessException);
            } finally {
                updateConnectionInfo(connectionSuccess);
                if (!success && response != null) {
                    response.onError();
                }
                if (response != null) {
                    response.after();
                }
            }
        }
    }

    private <T> void fireRequestWithPersistentAuth(Request<T> request, Response<T> response) {
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

        boolean connectionSuccess = false;
        if (result != RestCallResult.CONNECTION_ERROR) {
            connectionSuccess = true;
        }
        updateConnectionInfo(connectionSuccess);
    }

    private void updateConnectionInfo(boolean success) {
        ConnectionInfo connectionInfo;
        boolean prevSuccess = true;

        Collection<ConnectionInfo> connectionInfos = provider.query(ConnectionInfo.class).all();
        int size = connectionInfos.size();
        if (size != 0) {
            connectionInfo = connectionInfos.iterator().next();
            if (connectionInfo.getState() != ConnectionInfo.State.OK) {
                prevSuccess = false;
            }
        } else {
            connectionInfo = new ConnectionInfo();
        }
        Timestamp time = new Timestamp(System.currentTimeMillis());

        if (!success) {
            connectionInfo.update(ConnectionInfo.State.FAILED, time);
        } else {
            connectionInfo.update(ConnectionInfo.State.OK, time);
        }
        //update in DB
        if (size != 0) {
            provider.batch().update(connectionInfo).apply();
        } else {
            provider.batch().insert(connectionInfo).apply();
        }

        //show Notification
        if (!success && prevSuccess && PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SettingsActivity.KEY_CONNECTION_NOTIFICATION, false)) {
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
    }

    private <T> RestCallResult doFireRequestWithPersistentAuth(Request<T> request, Response<T> response) {
        AccountManagerFuture<Bundle> resp = accountManager.getAuthToken(MovirtAuthenticator.MOVIRT_ACCOUNT, MovirtAuthenticator.AUTH_TOKEN_TYPE, null, false, null, null);

        boolean success = false;

        try {
            Bundle result = resp.getResult();
            if (result.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);

                if (TextUtils.isEmpty(authToken)) {
                    fireConnectionError("Empty auth token");
                } else {
                    restClient.setCookie(JSESSIONID, authToken);
                    restClient.setAuthentication(new HttpAuthentication() {
                        @Override
                        public String getHeaderValue() {
                            // empty authentication - e.g. not the basic one
                            return "";
                        }
                    });

                    updateClientBeforeCall();

                    setPersistentAuthHeaders();

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
                            fireConnectionError(e);
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
                            fireConnectionError(e);
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
            fireConnectionError(e);
            if (e instanceof ResourceAccessException) {
                return RestCallResult.CONNECTION_ERROR;
            }
        }

        if (!success) {
            return RestCallResult.OTHER_ERROR;
        } else {
            return RestCallResult.SUCCESS;
        }

    }

    private void setPersistentAuthHeaders() {
        restClient.setHeader("Session-TTL", "120"); // 2h
        restClient.setHeader("Prefer", "persistent-auth, csrf-protection");
    }

    private void updateClientBeforeCall() {
        restClient.setHeader(FILTER, Boolean.toString(!authenticator.hasAdminPermissions()));
        requestFactory.setCertificateHandlingMode(authenticator.getCertHandlingStrategy());
        restClient.setRootUrl(authenticator.getApiUrl());
    }

    private int asIntWithDefault(String key, String defaultResult) {
        String maxEventsLocallyStr = PreferenceManager.getDefaultSharedPreferences(app).getString(key, defaultResult);
        try {
            return Integer.parseInt(maxEventsLocallyStr);
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultResult);
        }
    }

    private void fireConnectionError(Exception e) {
        String msg = e.getMessage();
        if (e instanceof HttpClientErrorException) {

            String responseBody = ((HttpClientErrorException) e).getResponseBodyAsString();
            if (!TextUtils.isEmpty(responseBody)) {

                try {
                    ErrorBody errorBody = mapper.readValue(((HttpClientErrorException) e).getResponseBodyAsByteArray(), ErrorBody.class);
                    msg = msg + " " + errorBody.fault.reason + " " + errorBody.fault.detail;
                } catch (IOException e1) {
                    msg = msg + ": " + responseBody;
                }

            }
        }

        fireConnectionError(String.format(errorMsg, msg));
    }

    private void fireConnectionError(String msg) {
        Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
        intent.putExtra(Broadcasts.Extras.CONNECTION_FAILURE_REASON, msg);
        context.sendBroadcast(intent);
    }

    enum RestCallResult {
        SUCCESS,
        AUTH_ERROR,
        CONNECTION_ERROR,
        OTHER_ERROR
    }

    public interface Request<T> {
        T fire();
    }

    public interface Response<T> {
        void before();

        void onResponse(T t) throws RemoteException;

        void onError();

        void after();
    }

    private interface WrapPredicate<E> {
        boolean toWrap(E entity);
    }

    public static abstract class SimpleResponse<T> implements Response<T> {

        @Override
        public void before() {
            // do nothing
        }

        @Override
        public void onResponse(T t) throws RemoteException {
            // do nothing
        }

        @Override
        public void onError() {
            // do nothing
        }

        @Override
        public void after() {
            // do nothing
        }
    }

    /**
     * Composes multiple {@link Response} objects and invokes their callbacks in specified order
     */
    public static class CompositeResponse<T> implements Response<T> {

        private final Response<T>[] responses;

        @SafeVarargs
        public CompositeResponse(Response<T>... responses) {
            this.responses = responses;
        }

        @Override
        public void before() {
            for (Response<T> response : responses) {
                if (response != null) {
                    response.before();
                }
            }
        }

        @Override
        public void onResponse(T t) throws RemoteException {
            for (Response<T> response : responses) {
                if (response != null) {
                    response.onResponse(t);
                }
            }
        }

        @Override
        public void onError() {
            for (Response<T> response : responses) {
                if (response != null) {
                    response.onError();
                }
            }
        }

        @Override
        public void after() {
            for (Response<T> response : responses) {
                if (response != null) {
                    response.after();
                }
            }
        }
    }
}
