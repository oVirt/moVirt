package org.ovirt.mobile.movirt.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.ovirt.mobile.movirt.MoVirtApp;
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
import org.ovirt.mobile.movirt.sync.SyncUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class OVirtClient implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = OVirtClient.class.getSimpleName();
    private static final String CPU_PERCENTAGE_STAT = "cpu.current.total";
    private static final String TOTAL_MEMORY_STAT = "memory.installed";
    private static final String USED_MEMORY_STAT = "memory.used";
    // for debugging purposes only
    public static final String DEFAULT_ENDPOINT = "http://10.0.2.2:8080/ovirt-engine/api";
    public static final String DEFAULT_USERNAME = "admin@internal";
    public static final String DEFAULT_PASSWORD = "123456";
    public static final Boolean DEFAULT_HTTPS = false;
    public static final Boolean DEFAULT_ADMIN_PRIVILEGE = false;
    public static final String DEFAULT_POLLING_INTERVAL = "60";

    @RestService
    OVirtRestClient restClient;

    @Bean
    ErrorHandler restErrorHandler;

    @Bean
    OvirtSimpleClientHttpRequestFactory requestFactory;

    @Bean
    TriggerResolverFactory triggerResolverFactory;

    public void startVm(Vm vm) {
        restClient.startVm(new Action(), vm.getId());

        SyncUtils.triggerRefresh();
    }

    public void stopVm(Vm vm) {
        restClient.stopVm(new Action(), vm.getId());

        SyncUtils.triggerRefresh();
    }

    public void rebootVm(Vm vm) {
        restClient.rebootVm(new Action(), vm.getId());

        SyncUtils.triggerRefresh();
    }

    public ExtendedVm  getVm(Vm vm) {
        return restClient.getVm(vm.getId());
    }

    public ActionTicket getConsoleTicket(Vm vm) {
        return restClient.getConsoleTicket(new Action(), vm.getId());
    }

    public Disks getDiskData(String id) {
        Disks loadedDisks = null;
        loadedDisks = restClient.getDiskData(id);

        return loadedDisks;
    }

    public List<Vm> getVms() {
        Vms loadedVms = null;
        if (hasAdminPrivilege()) {
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

    private void updateVmsStatistics(List<Vm> vms) {
        TriggerResolver<Vm> resolver = triggerResolverFactory.getResolverForEntity(Vm.class);

        for (Vm vm : vms) {
            updateVmStatistics(vm, resolver);
        }
    }

    public VmStatistics getVmStatistics(Vm vm) {
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

    private void updateVmStatistics(Vm vm, TriggerResolver<Vm> resolver) {
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

        VmStatistics statistics = getVmStatistics(vm);
        if (statistics != null) {
            vm.setCpuUsage(statistics.getCpuUsage());
            vm.setMemoryUsage(statistics.getMemoryUsage());
        }
    }

    private BigDecimal getStatisticValueByName(String name, List<Statistic> statistics) {
        for (Statistic statistic : statistics) {
            if (name.equals(statistic.name)) {
                return new BigDecimal(statistic.values.value.get(0).datum);
            }
        }
        return BigDecimal.ZERO;
    }

    public List<Cluster> getClusters() {
        Clusters loadedClusters = restClient.getClusters();
        if (loadedClusters == null) {
            return new ArrayList<>();
        }

        return mapRestWrappers(loadedClusters.cluster, null);
    }

    public List<Event> getEventsSince(final int lastEventId) {
        Events loadedEvents = null;

        if (hasAdminPrivilege()) {
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

    @App
    MoVirtApp app;

    @AfterInject
    void initClient() {
        restClient.setRestErrorHandler(restErrorHandler);
        restClient.setHeader("Accept-Encoding", "gzip");
        updateConnection();
        restClient.getRestTemplate().setRequestFactory(requestFactory);
        registerSharedPreferencesListener();
    }

    private void updateConnection() {
        updateRootUrlFromSettings();
        updateAuthenticationFromSettings();
        updateDisableHttpsChecking();
        updateAdminPrivilegeStatus();
        updatePollingInterval();
        SyncUtils.triggerRefresh();
    }

    private void updateRootUrlFromSettings() {
        String endpoint = PreferenceManager.getDefaultSharedPreferences(app).getString("endpoint", DEFAULT_ENDPOINT);
        restClient.setRootUrl(endpoint);
        Log.i(TAG, "Updating root url to: " + endpoint);
    }

    private void updateAuthenticationFromSettings() {
        String username = PreferenceManager.getDefaultSharedPreferences(app).getString("username", DEFAULT_USERNAME);
        String password = PreferenceManager.getDefaultSharedPreferences(app).getString("password", DEFAULT_PASSWORD);
        Log.i(TAG, "Updating username to: " + username);
        Log.i(TAG, "Updating password to: " + password);
        restClient.setHttpBasicAuth(username, password);
    }

    private void updateAdminPrivilegeStatus() {
        Boolean adminPrivilegeStatus = hasAdminPrivilege();
        Log.i(TAG, "Updating admin privilege status to: " + adminPrivilegeStatus);
        restClient.setHeader("Filter", String.valueOf(!adminPrivilegeStatus));
    }

    private void updatePollingInterval() {
        int pollingInterval = asIntWithDefault("polling_interval", DEFAULT_POLLING_INTERVAL);
        Log.i(TAG,"Updating Polling Interval to :" + pollingInterval);
        SyncUtils.updatePollingInterval(pollingInterval);
    }

    private void updateDisableHttpsChecking() {
        Boolean disableHttpsChecking = PreferenceManager.getDefaultSharedPreferences(app).getBoolean("disableHttpsChecking", DEFAULT_HTTPS);
        requestFactory.setIgnoreHttps(disableHttpsChecking);
        Log.i(TAG, "Https Disabled updated: " + disableHttpsChecking);
    }

    private void registerSharedPreferencesListener() {
        app.getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS).registerOnSharedPreferenceChangeListener(this);
        PreferenceManager.getDefaultSharedPreferences(app).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("endpoint")) {
            updateRootUrlFromSettings();
        }
        if (key.equals("username") || key.equals("password")) {
            updateAuthenticationFromSettings();
        }
        if (key.equals("admin_privilege")){
            updateAdminPrivilegeStatus();
        }
        if (key.equals("polling_interval")){
            updatePollingInterval();
        }
        if (key.equals("disableHttpsChecking")) {
            updateDisableHttpsChecking();
        }

        Log.i(TAG, key + " changed");
        SyncUtils.triggerRefresh();
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

    private Boolean hasAdminPrivilege() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("admin_privilege", DEFAULT_ADMIN_PRIVILEGE);
    }
}
