package org.ovirt.mobile.movirt.rest;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
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
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.MainActivity_;
import org.ovirt.mobile.movirt.util.NotificationHelper;
import org.ovirt.mobile.movirt.util.SharedPreferencesHelper;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class OVirtClient {
    public static final String JSESSIONID = "JSESSIONID";
    public static final String FILTER = "Filter";
    public static final String PREFER = "Prefer";
    public static final String SESSION_TTL = "Session-TTL";
    public static final String VERSION = "Version";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
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

    public boolean isApiV3() {
        return authenticator.isApiV3();
    }

    public <E, U extends RestEntityWrapper<E>> List<E> mapToEntities(RestEntityWrapperList<U> wrappersList) {
        return mapToEntities(wrappersList, null);
    }

    public <E, U extends RestEntityWrapper<E>> List<E> mapToEntities(RestEntityWrapperList<U> wrappersList, WrapPredicate<U> predicate) {
        if (wrappersList == null) {
            return Collections.emptyList();
        }

        List<U> wrappers = wrappersList.getList();

        if (wrappers == null) {
            return Collections.emptyList();
        }

        List<E> entities = new ArrayList<>();
        for (U rest : wrappers) {
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
                Action action = isApiV3() ? new org.ovirt.mobile.movirt.rest.v3.ActionMigrate(hostId) :
                        new org.ovirt.mobile.movirt.rest.v4.ActionMigrate(hostId);
                restClient.migrateVmToHost(action, vmId);
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
                org.ovirt.mobile.movirt.rest.Vm vm = isApiV3() ? restClient.getVmV3(vmId) : restClient.getVmV4(vmId);
                return vm.toEntity();
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

    public void deleteSnapshot(final String vmId, final String snapshotId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.deleteSnapshot(vmId, snapshotId);
                return null;
            }
        }, response);
    }

    public void restoreSnapshot(final SnapshotAction snapshotAction, final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                String snapshotId = snapshotAction.snapshot.id;
                SnapshotAction restAction = new SnapshotAction(snapshotAction.restore_memory);

                restClient.restoreSnapshot(restAction, vmId, snapshotId);
                return null;
            }
        }, response);
    }

    public void previewSnapshot(final SnapshotAction snapshotAction, final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                if (isApiV3()) {
                    restClient.previewSnapshotV3(snapshotAction, vmId);
                } else {
                    restClient.previewSnapshotV4(snapshotAction, vmId);
                }
                return null;
            }
        }, response);
    }

    public void createSnapshot(final org.ovirt.mobile.movirt.rest.Snapshot snapshot, final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                restClient.createSnapshot(snapshot, vmId);
                return null;
            }
        }, response);
    }

    public void commitSnapshot(final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                if (isApiV3()) {
                    restClient.commitSnapshotV3(new Action(), vmId);
                } else {
                    restClient.commitSnapshotV4(new Action(), vmId);
                }
                return null;
            }
        }, response);
    }

    public void undoSnapshot(final String vmId, Response<Void> response) {
        fireRestRequest(new Request<Void>() {
            @Override
            public Void fire() {
                if (isApiV3()) {
                    restClient.undoSnapshotV3(new Action(), vmId);
                } else {
                    restClient.undoSnapshotV4(new Action(), vmId);
                }
                return null;
            }
        }, response);
    }

    @NonNull
    public Request<Host> getHostRequest(final String hostId) {
        return new Request<Host>() {
            @Override
            public Host fire() {
                org.ovirt.mobile.movirt.rest.Host wrapper = isApiV3() ?
                        restClient.getHostV3(hostId) : restClient.getHostV4(hostId);
                return wrapper.toEntity();
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
                org.ovirt.mobile.movirt.rest.StorageDomain wrapper = isApiV3() ?
                        restClient.getStorageDomainV3(storageDomainId) :
                        restClient.getStorageDomainV4(storageDomainId);
                return wrapper.toEntity();
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

    public Request<Disk> getDiskRequest(final String vmId, final String id) {
        return getDiskRequest(vmId, null, id);
    }

    @NonNull
    public Request<Disk> getDiskRequest(final String vmId, final String snapshotId, final String id) {
        final boolean isSnapshotEmbedded = snapshotId != null;

        return new Request<Disk>() {
            @Override
            public Disk fire() {
                org.ovirt.mobile.movirt.rest.Disk wrapper;
                Disk entity;

                if (isSnapshotEmbedded) {
                    if (isApiV3()) {
                        wrapper = restClient.getDiskV3(vmId, snapshotId, id);
                    } else {
                        wrapper = restClient.getDiskV4(vmId, snapshotId, id);
                    }
                    entity = wrapper.toEntity();
                    setVmId(entity, vmId);
                } else {
                    if (isApiV3()) {
                        wrapper = restClient.getDiskV3(vmId, id);
                    } else {
                        wrapper = restClient.getDiskV4(vmId, id);
                    }
                    entity = wrapper.toEntity();
                }

                return entity;
            }
        };
    }

    public Request<List<Disk>> getDisksRequest(final String vmId) {
        return getDisksRequest(vmId, null);
    }

    public Request<List<Disk>> getDisksRequest(final String vmId, final String snapshotId) {
        final boolean isSnapshotEmbedded = snapshotId != null;

        return new Request<List<Disk>>() {
            @Override
            public List<Disk> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.Disk> wrappers;
                List<Disk> entities;

                if (isSnapshotEmbedded) {

                    if (isApiV3()) {
                        wrappers = restClient.getDisksV3(vmId, snapshotId);
                    } else {
                        wrappers = restClient.getDisksV4(vmId, snapshotId);
                    }
                    entities = mapToEntities(wrappers);
                    setVmId(entities, vmId);
                } else {
                    if (isApiV3()) {
                        wrappers = restClient.getDisksV3(vmId);
                    } else {
                        wrappers = restClient.getDisksV4(vmId);
                    }
                    entities = mapToEntities(wrappers);
                }

                return entities;
            }
        };
    }

    @UiThread
    void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public void getClusters(Response<List<Cluster>> response) {
        fireRestRequest(new Request<List<Cluster>>() {
            @Override
            public List<Cluster> fire() {
                if (isApiV3()) {
                    return mapToEntities(restClient.getClustersV3());
                }
                return mapToEntities(restClient.getClustersV4());
            }
        }, response);
    }

    public void getDataCenters(Response<List<DataCenter>> response) {
        fireRestRequest(new Request<List<DataCenter>>() {
            @Override
            public List<DataCenter> fire() {
                if (isApiV3()) {
                    return mapToEntities(restClient.getDataCentersV3());
                }
                return mapToEntities(restClient.getDataCentersV4());
            }
        }, response);
    }

    public Request<Nic> getNicRequest(final String vmId, final String id) {
        return getNicRequest(vmId, null, id);
    }

    @NonNull
    public Request<Nic> getNicRequest(final String vmId, final String snapshotId, final String id) {
        return new Request<Nic>() {
            @Override
            public Nic fire() {
                org.ovirt.mobile.movirt.rest.Nic wrapper;
                Nic entity;

                if (snapshotId == null) {
                    if (isApiV3()) {
                        wrapper = restClient.getNicV3(vmId, id);
                    } else {
                        wrapper = restClient.getNicV4(vmId, id);
                    }
                    entity = wrapper.toEntity();
                    setVmId(entity, vmId);
                } else {
                    if (isApiV3()) {
                        wrapper = restClient.getNicV3(vmId, snapshotId, id);
                    } else {
                        wrapper = restClient.getNicV4(vmId, snapshotId, id);
                    }
                    entity = wrapper.toEntity();
                }

                return entity;
            }
        };
    }

    public Request<List<Nic>> getNicsRequest(final String vmId) {
        return getNicsRequest(vmId, null);
    }

    public Request<List<Nic>> getNicsRequest(final String vmId, final String snapshotId) {
        return new Request<List<Nic>>() {
            @Override
            public List<Nic> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.Nic> wrappers;
                List<Nic> entities;

                if (snapshotId == null) {

                    if (isApiV3()) {
                        wrappers = restClient.getNicsV3(vmId);
                    } else {
                        wrappers = restClient.getNicsV4(vmId);
                    }
                    entities = mapToEntities(wrappers);
                    setVmId(entities, vmId);
                } else {
                    if (isApiV3()) {
                        wrappers = restClient.getNicsV3(vmId, snapshotId);
                    } else {
                        wrappers = restClient.getNicsV4(vmId, snapshotId);
                    }
                    entities = mapToEntities(wrappers);
                }

                return entities;
            }
        };
    }

    public Request<List<Host>> getHostsRequest() {
        return new Request<List<Host>>() {
            @Override
            public List<Host> fire() {
                if (isApiV3()) {
                    return mapToEntities(restClient.getHostsV3());
                }
                return mapToEntities(restClient.getHostsV4());
            }
        };
    }

    public Request<List<Vm>> getVmsRequest() {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(app);

        return new Request<List<Vm>>() {
            @Override
            public List<Vm> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.Vm> wrappers;

                if (authenticator.hasAdminPermissions()) {
                    int maxVms = sharedPreferencesHelper.getMaxVms();
                    String query = sharedPreferences.getString("vms_search_query", "");
                    if (StringUtils.isEmpty(query)) {
                        wrappers = isApiV3() ? restClient.getVmsV3(maxVms) :
                                restClient.getVmsV4(maxVms);
                    } else {
                        wrappers = isApiV3() ? restClient.getVmsV3(query, maxVms) :
                                restClient.getVmsV4(query, maxVms);
                    }
                } else {
                    wrappers = isApiV3() ? restClient.getVmsV3(-1) :
                            restClient.getVmsV4(-1);
                }

                return mapToEntities(wrappers);
            }
        };
    }

    public Request<List<StorageDomain>> getStorageDomainsRequest() {
        return new Request<List<StorageDomain>>() {
            @Override
            public List<StorageDomain> fire() {
                if (isApiV3()) {
                    return mapToEntities(restClient.getStorageDomainsV3());
                }
                return mapToEntities(restClient.getStorageDomainsV4());

            }
        };
    }

    public Request<List<Snapshot>> getSnapshotsRequest(final String vmId) {
        return new Request<List<Snapshot>>() {
            @Override
            public List<Snapshot> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.Snapshot> wrappers;
                List<Snapshot> entities;

                if (isApiV3()) {
                    wrappers = restClient.getSnapshotsV3(vmId);
                } else {
                    wrappers = restClient.getSnapshotsV4(vmId);
                }

                entities = mapToEntities(wrappers);
                setVmId(entities, vmId); // Active VM Snapshot doesn't include this

                return entities;
            }
        };
    }

    public Request<Snapshot> getSnapshotRequest(final String vmId, final String snapshotId) {
        return new Request<Snapshot>() {
            @Override
            public Snapshot fire() {
                org.ovirt.mobile.movirt.rest.Snapshot wrapper;
                Snapshot entity;

                if (isApiV3()) {
                    wrapper = restClient.getSnapshotV3(vmId, snapshotId);
                } else {
                    wrapper = restClient.getSnapshotV4(vmId, snapshotId);
                }

                entity = wrapper.toEntity();
                setVmId(entity, vmId);

                return entity;
            }
        };
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
                    return Collections.emptyList();
                }

                return mapToEntities(loadedEvents, new WrapPredicate<org.ovirt.mobile.movirt.rest.Event>() {
                    @Override
                    public boolean toWrap(org.ovirt.mobile.movirt.rest.Event entity) {
                        return entity.id > lastEventId;
                    }
                });
            }
        }, response);
    }

    private <E extends OVirtContract.HasVm> void setVmId(E entity, String vmId) {
        if (entity != null && !StringUtils.isEmpty(vmId)) {
            entity.setVmId(vmId);
        }
    }

    private <E extends OVirtContract.HasVm> void setVmId(List<E> entities, String vmId) {
        if (entities != null && !StringUtils.isEmpty(vmId)) {
            for (E entity : entities) {
                entity.setVmId(vmId);
            }
        }
    }

    @AfterInject
    void initClient() {
        restClient.setHeader(ACCEPT_ENCODING, "gzip");
        setupVersionHeader(authenticator.getApiMajorVersion());

        restClient.getRestTemplate().setRequestFactory(requestFactory);
    }

    public void setupVersionHeader(String version) {
        restClient.setHeader(VERSION, version);
    }

    private void setPersistentV3AuthHeaders() {
        restClient.setHeader(SESSION_TTL, "120"); // 2h
        restClient.setHeader(PREFER, "persistent-auth, csrf-protection");
    }

    private void resetClientSettings() {
        restClient.setHeader(SESSION_TTL, "");
        restClient.setHeader(PREFER, "");
        restClient.setCookie(JSESSIONID, "");
        restClient.setAuthentication(new HttpAuthentication() {
            @Override
            public String getHeaderValue() {
                // empty authentication - e.g. not the basic one
                return "";
            }
        });
    }

    private void updateClientBeforeCall() {
        restClient.setHeader(FILTER, Boolean.toString(!authenticator.hasAdminPermissions()));
        requestFactory.setCertificateHandlingMode(authenticator.getCertHandlingStrategy());
        restClient.setRootUrl(authenticator.getApiUrl());
    }

    /**
     * @param username username
     * @param password password
     * @return auth token depending on API version
     */
    public String login(String username, String password) {
        String token = "";

        resetClientSettings();
        setupVersionHeader("");
        updateClientBeforeCall();
        restClient.setRootUrl(authenticator.getBaseUrl());
        try {
            token = restClient.loginV4(username, password).getAccessToken();
        } catch (Exception x) {// 405 Method Not Allowed - old API
        }
        restClient.setRootUrl(authenticator.getApiUrl());

        boolean oldApi = StringUtils.isEmpty(token);
        restClient.setCookie(JSESSIONID, ""); // v4 may set JSESSIONID

        if (oldApi) {
            restClient.setHttpBasicAuth(username, password);
            setPersistentV3AuthHeaders();
        } else {
            restClient.setBearerAuth(token);
        }

        Api api = restClient.loginV3();
        authenticator.setApiMajorVersion(api);
        setupVersionHeader(authenticator.getApiMajorVersion());

        if (oldApi && api != null) { // check for api because v4 may set JSESSIONID even if login was unsuccessful
            token = restClient.getCookie(JSESSIONID);
        }

        resetClientSettings();
        return token;
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

        if (result != RestCallResult.CONNECTION_ERROR) {
            updateConnectionInfo(true);
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
                    fireOtherConnectionError("Empty auth token");
                } else {
                    resetClientSettings();
                    updateClientBeforeCall();
                    if (isApiV3()) {
                        restClient.setCookie(JSESSIONID, authToken);
                        setPersistentV3AuthHeaders();
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
                            fireOtherConnectionError(e);
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
            fireOtherConnectionError(e);
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

    private void fireOtherConnectionError(Exception e) {
        String msg = e.getMessage();
        if (e instanceof HttpClientErrorException) {
            HttpStatus statusCode = ((HttpClientErrorException) e).getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                msg = msg + ": " + "oVirt-engine is not found on " + restClient.getRootUrl();
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

        private List<Response<T>> responses;

        @SafeVarargs
        public CompositeResponse(Response<T>... responses) {
            this.responses = new ArrayList<>(Arrays.asList(responses));
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

        public void addResponse(Response<T> response) {
            responses.add(response);
        }
    }
}
