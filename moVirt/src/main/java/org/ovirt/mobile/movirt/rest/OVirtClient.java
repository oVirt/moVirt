package org.ovirt.mobile.movirt.rest;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.VmStatistics;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.condition.CpuThresholdCondition;
import org.ovirt.mobile.movirt.model.condition.MemoryThresholdCondition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolverFactory;
import org.ovirt.mobile.movirt.sync.EventsHandler;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity;
import org.ovirt.mobile.movirt.ui.AuthenticatorActivity_;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class OVirtClient {
    private static final String TAG = OVirtClient.class.getSimpleName();

    private static final String CPU_PERCENTAGE_STAT = "cpu.current.total";
    private static final String TOTAL_MEMORY_STAT = "memory.installed";
    private static final String USED_MEMORY_STAT = "memory.used";
    public static final String JSESSIONID = "JSESSIONID";
    public static final String FILTER = "Filter";
    public static final String PREFER = "Prefer";

    @RestService
    OVirtRestClient restClient;

    @Bean
    OvirtSimpleClientHttpRequestFactory requestFactory;

    @Bean
    TriggerResolverFactory triggerResolverFactory;

    @RootContext
    Context context;

    @SystemService
    AccountManager accountManager;

    @Bean
    MovirtAuthenticator authenticator;

    @App
    MoVirtApp app;

    @StringRes(R.string.rest_request_failed)
    String errorMsg;

    public void startVm(final Vm vm) {
        fireRestRequest(new Request<Object>() {
            @Override
            public Object fire() {
                restClient.startVm(new Action(), vm.getId());
                return null;
            }
        }, null);
    }

    public void stopVm(final Vm vm) {
        fireRestRequest(new Request<Object>() {
            @Override
            public Object fire() {
                restClient.stopVm(new Action(), vm.getId());
                return null;
            }
        }, null);

    }

    public void rebootVm(final Vm vm) {
        fireRestRequest(new Request<Object>() {
            @Override
            public Object fire() {
                restClient.rebootVm(new Action(), vm.getId());
                return null;
            }
        }, null);
    }

    public void getVm(final Vm vm, Response<ExtendedVm> response) {
        fireRestRequest(new Request<ExtendedVm>() {
            @Override
            public ExtendedVm fire() {
                return restClient.getVm(vm.getId());
            }
        }, response);
    }

    public void getConsoleTicket(final Vm vm, Response<ActionTicket> response) {
        fireRestRequest(new Request<ActionTicket>() {
            @Override
            public ActionTicket fire() {
                return restClient.getConsoleTicket(new Action(), vm.getId());
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
                List<Vm> vms = mapRestWrappers(loadedVms.vm, null);
                updateVmsStatistics(vms);

                return vms;
            }
        }, response);
    }

    private void updateVmsStatistics(List<Vm> vms) {
        TriggerResolver<Vm> resolver = triggerResolverFactory.getResolverForEntity(Vm.class);

        for (Vm vm : vms) {
            updateVmStatistics(vm, resolver);
        }
    }

    public void getVmStatistics(final Vm vm, Response<VmStatistics> response) {
        fireRestRequest(new Request<VmStatistics>() {
            @Override
            public VmStatistics fire() {
                VmStatistics res = new VmStatistics();
                final List<Statistic> statistics = restClient.getVmStatistics(vm.getId()).statistic;

                if (statistics != null) {
                    BigDecimal cpu = getStatisticValueByName(CPU_PERCENTAGE_STAT, statistics);
                    BigDecimal totalMemory = getStatisticValueByName(TOTAL_MEMORY_STAT, statistics);
                    BigDecimal usedMemory = getStatisticValueByName(USED_MEMORY_STAT, statistics);

                    res.setCpuUsage(cpu.doubleValue());
                    if (BigDecimal.ZERO.equals(totalMemory)) {
                        res.setMemoryUsage(0);
                    } else {
                        res.setMemoryUsage(100 * usedMemory.divide(totalMemory, 3, RoundingMode.HALF_UP).doubleValue());
                    }
                    return res;
                }

                return null;
            }
        }, response);

    }

    private void updateVmStatistics(final Vm vm, TriggerResolver<Vm> resolver) {
        List<Trigger<Vm>> triggersForEntity = resolver.getTriggersForEntity(vm);
        if (triggersForEntity.isEmpty()) {
            return;
        }

        boolean needsUpdate = false;
        for (Trigger<Vm> trigger : triggersForEntity) {
            Condition<Vm> condition = trigger.getCondition();
            if (condition instanceof CpuThresholdCondition || condition instanceof MemoryThresholdCondition) {
                needsUpdate = true;
                break;
            }
        }

        if (!needsUpdate) {
            return;
        }

        getVmStatistics(vm, new SimpleResponse<VmStatistics>() {
            @Override
            public void onResponse(VmStatistics statistics) throws RemoteException {
                if (statistics != null) {
                    vm.setCpuUsage(statistics.getCpuUsage());
                    vm.setMemoryUsage(statistics.getMemoryUsage());
                }
            }
        });
    }

    private BigDecimal getStatisticValueByName(String name, List<Statistic> statistics) {
        for (Statistic statistic : statistics) {
            if (name.equals(statistic.name)) {
                return new BigDecimal(statistic.values.value.get(0).datum);
            }
        }
        return BigDecimal.ZERO;
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

    public String login(String apiUrl, String username, String password, boolean disableHttps, boolean hasAdminPrivileges) {
        setPersistentAuthHeaders();
        restClient.setRootUrl(apiUrl);
        restClient.setHttpBasicAuth(username, password);
        restClient.setCookie("JSESSIONID", "");
        requestFactory.setIgnoreHttps(disableHttps);
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
            } finally {
                if (!success && response != null) {
                    response.onError();
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
        }
        
        if (!success) {
            return RestCallResult.OTHER_ERROR;
        } else {
            return RestCallResult.SUCCESS;
        }

    }

    enum RestCallResult {
        SUCCESS,
        AUTH_ERROR,
        OTHER_ERROR
    }

    private void setPersistentAuthHeaders() {
        restClient.setHeader("Session-TTL", "120"); // 2h
        restClient.setHeader("Prefer", "persistent-auth, csrf-protection");
    }

    private void updateClientBeforeCall() {
        restClient.setHeader(FILTER, Boolean.toString(!authenticator.hasAdminPermissions()));
        requestFactory.setIgnoreHttps(authenticator.disableHttps());
        restClient.setRootUrl(authenticator.getApiUrl());
    }

    public static interface Request<T> {
        T fire();
    }

    public static interface Response<T> {
        void before();

        void onResponse(T t) throws RemoteException;

        void onError();
    }

    public static abstract class SimpleResponse<T> implements Response<T> {

        @Override
        public void before() {
            // do nothing
        }

        @Override
        public void onError() {
            // do nothing
        }
    }

    private static interface WrapPredicate<E> {
        boolean toWrap(E entity);
    }

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

    private int asIntWithDefault(String key, String defaultResult) {
        String maxEventsLocallyStr = PreferenceManager.getDefaultSharedPreferences(app).getString(key, defaultResult);
        try {
            return Integer.parseInt(maxEventsLocallyStr);
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultResult);
        }
    }

    private void fireConnectionError(Exception e) {
        fireConnectionError(String.format(errorMsg, e.getMessage()));
    }

    private void fireConnectionError(String msg) {
        Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
        intent.putExtra(Broadcasts.Extras.CONNECTION_FAILURE_REASON, msg);
        context.sendBroadcast(intent);
    }
}
