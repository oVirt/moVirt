package org.ovirt.mobile.movirt.rest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.ovirt.mobile.movirt.AppPrefs_;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.sync.SyncUtils;

import java.math.BigDecimal;
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

    @RestService
    OVirtRestClient restClient;

    @Bean
    ErrorHandler restErrorHandler;

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

    public List<Vm> getVms() {
     //   Log.d(TAG, "Getting VMs using " + prefs.username().get() + " and " + prefs.password().get());
        List<Vm> vms = mapRestWrappers(restClient.getVms().vm);
        updateVmsStatistics(vms);

        return vms;
    }

    private void updateVmsStatistics(List<Vm> vms) {
        for (Vm vm : vms) {
            updateVmStatistics(vm);
        }
    }

    private void updateVmStatistics(Vm vm) {
        final List<Statistic> statistics = restClient.getVmStatistics(vm.getId()).statistic;
        BigDecimal cpu = getStatisticValueByName(CPU_PERCENTAGE_STAT, statistics);
        BigDecimal totalMemory = getStatisticValueByName(TOTAL_MEMORY_STAT, statistics);
        BigDecimal usedMemory = getStatisticValueByName(USED_MEMORY_STAT, statistics);

        vm.setCpuUsage(cpu.doubleValue());
        if (BigDecimal.ZERO.equals(totalMemory)) {
            vm.setMemoryUsage(0);
        } else {
            vm.setMemoryUsage(100 * usedMemory.divide(totalMemory).doubleValue());
        }
    }

    private BigDecimal getStatisticValueByName(String name, List<Statistic> statistics) {
        for (Statistic statistic : statistics) {
            if (name.equals(statistic.name)) {
                return new BigDecimal(statistic.values.value.get(0).datum);
            }
        }
        return null;
    }

    public List<Cluster> getClusters() {
        return mapRestWrappers(restClient.getClusters().cluster);
    }

    public List<Event> getVmEvents(String vmId) {
        return mapRestWrappers(restClient.getEvents("Vms.id=" + vmId).event);
    }

    public List<Event> getEventsSince(int lastEventId) {
        return filterLogEvents(mapRestWrappers(restClient.getEventsSince(Integer.toString(lastEventId)).event));
    }

    private static List<Event> filterLogEvents(List<Event> events) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (e.getCode() != Event.Codes.USER_VDC_LOGIN && e.getCode() != Event.Codes.USER_VDC_LOGOUT) {
                result.add(e);
            }
        }
        return result;
    }

    @App
    MoVirtApp app;

    @AfterInject
    void initClient() {
        restClient.setRestErrorHandler(restErrorHandler);
        updateConnection();
        registerSharedPreferencesListener();
    }

    private void updateConnection() {
        updateRootUrlFromSettings();
        updateAuthenticationFromSettings();
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
        Log.i(TAG, key + " changed");
        SyncUtils.triggerRefresh();
    }

    private static <E, R extends RestEntityWrapper<E>> List<E> mapRestWrappers(List<R> wrappers) {
        List<E> entities = new ArrayList<>();
        for (R rest : wrappers) {
            entities.add(rest.toEntity());
        }
        return entities;
    }

    public void setContext(Activity context) {
        restErrorHandler.setContext(context);
    }
}
