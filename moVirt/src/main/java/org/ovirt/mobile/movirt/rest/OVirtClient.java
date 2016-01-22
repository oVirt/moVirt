package org.ovirt.mobile.movirt.rest;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
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
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

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

    @Bean
    SharedPreferencesHelper sharedPreferencesHelper;

    private <E, R extends RestEntityWrapper<E>> List<E> mapRestWrappers(List<R> wrappers, WrapPredicate<R> predicate) {
        List<E> entities = new ArrayList<>();
        if (wrappers == null) {
            return entities;
        }

        for (R rest : wrappers) {
            try {
                if (predicate == null || predicate.toWrap(rest)) {
                    entities.add(rest.toEntity());
                }
            } catch (Exception e) {
                // showing only as a toast since this problem may persist and we don't want to flood the user with messages like this as dialogs...
                showToast("Error parsing rest response, ignoring: " + rest.toString() + " error: " + e.getMessage());
            }
        }
        return entities;
    }

    public void startVm(final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.startVm(new Action(), vmId);
                return null;
            }
        }, response);
    }

    public void stopVm(final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.stopVm(new Action(), vmId);
                return null;
            }
        }, response);

    }

    public void rebootVm(final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.rebootVm(new Action(), vmId);
                return null;
            }
        }, response);
    }

    public void migrateVmToHost(final String vmId, final String hostId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.migrateVmToHost(new ActionMigrate(hostId), vmId);
                return null;
            }
        }, response);
    }

    public void migrateVmToDefaultHost(final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.migrateVmToHost(new Action(), vmId);
                return null;
            }
        }, response);
    }

    public void cancelMigration(final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.cancelMigration(new Action(), vmId);
                return null;
            }
        }, response);
    }

    @NonNull
    public Request<Vm> getVmRequest(final String vmId) {
        return new Request<Vm>() {
            @Override
            public Vm fire() {
                return restClient.getVm(vmId).toEntity();
            }
        };
    }

    public void getVm(final String vmId, Response<Vm> response) {
        fireRestRequest(getVmRequest(vmId), response);
    }

    public void activateHost(final String hostId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.activateHost(new Action(), hostId);
                return null;
            }
        }, response);
    }

    public void dectivateHost(final String hostId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.deactivateHost(new Action(), hostId);
                return null;
            }
        }, response);
    }

    @NonNull
    public Request<Host> getHostRequest(final String hostId) {
        return new Request<Host>() {
            @Override
            public Host fire() {
                return restClient.getHost(hostId).toEntity();
            }
        };
    }

    public void getHost(final String hostId, Response<Host> response) {
        fireRestRequest(getHostRequest(hostId), response);
    }

    @NonNull
    public Request<StorageDomain> getStorageDomainRequest(final String storageDomainId) {
        return new Request<StorageDomain>() {
            @Override
            public StorageDomain fire() {
                return restClient.getStorageDomain(storageDomainId).toEntity();
            }
        };
    }

    public void getStorageDomain(final String storageDomainId, Response<StorageDomain> response) {
        fireRestRequest(getStorageDomainRequest(storageDomainId), response);
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
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(app);
        fireRestRequest(new Request<List<Vm>>() {
            @Override
            public List<Vm> fire() {
                Vms loadedVms = null;
                if (authenticator.hasAdminPermissions()) {
                    int maxVms = sharedPreferencesHelper.getMaxVms();
                    String query = sharedPreferences.getString("vms_search_query", "");
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

    @UiThread
    void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
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
                    return new ArrayList<>();
                }

                return mapRestWrappers(loadedDataCenters.data_center, null);
            }
        }, response);
    }

    public void getStorageDomains(Response<List<StorageDomain>> response) {
        fireRestRequest(new Request<List<StorageDomain>>() {
            @Override
            public List<StorageDomain> fire() {
                StorageDomains loadedStorageDomains = restClient.getStorageDomains();
                if (loadedStorageDomains == null){
                    return new ArrayList<>();
                }

                return mapRestWrappers(loadedStorageDomains.storage_domain, null);
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
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(app);
        fireRestRequest(new Request<List<Event>>() {
            @Override
            public List<Event> fire() {
                Events loadedEvents = null;

                if (authenticator.hasAdminPermissions()) {
                    int maxEventsStored = sharedPreferencesHelper.getMaxEvents();
                    String query = sharedPreferences.getString("events_search_query", "");
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
    public synchronized <T> void fireRestRequest(final Request<T> request, final Response<T> response) {
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
        ConnectionInfo.State state;
        boolean prevFailed = false;
        boolean configured = sharedPreferencesHelper.isConnectionNotificationEnabled();
        Collection<ConnectionInfo> connectionInfos = provider.query(ConnectionInfo.class).all();
        int size = connectionInfos.size();

        if (size != 0) {
            connectionInfo = connectionInfos.iterator().next();
            if (connectionInfo.getState() == ConnectionInfo.State.FAILED) {
                prevFailed = true;
            }
        } else {
            connectionInfo = new ConnectionInfo();
        }

        if (!success) {
            state = ConnectionInfo.State.FAILED;
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

    private void fireConnectionError(Exception e) {
        String msg = e.getMessage();
        if (e instanceof HttpClientErrorException) {
            HttpStatus statusCode = ((HttpClientErrorException) e).getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                msg = msg + ": " + "oVirt-engine is not found on " + restClient.getRootUrl();
                fireConnectionError(String.format(errorMsg, msg));
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
