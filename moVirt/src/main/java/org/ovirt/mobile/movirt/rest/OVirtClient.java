package org.ovirt.mobile.movirt.rest;

import android.content.Context;
import android.content.SharedPreferences;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
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

    @RestService
    OVirtRestClient restClient;

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
        vm.setMemoryUsage(100 * usedMemory.divide(totalMemory).doubleValue());
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

    @Pref
    AppPrefs_ prefs;

    @App
    MoVirtApp app;

    @AfterInject
    void initClient() {
        updateRootUrlFromSettings();
        updateAuthenticationFromSettings();
        registerSharedPreferencesListener();
    }

    private void updateRootUrlFromSettings() {
        restClient.setRootUrl(prefs.endpoint().get());
    }

    private void updateAuthenticationFromSettings() {
        restClient.setHttpBasicAuth(prefs.username().get(), prefs.password().get());
    }

    private void registerSharedPreferencesListener() {
        app.getSharedPreferences("MyPrefs", Context.MODE_MULTI_PROCESS).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("endpoint")) {
            updateRootUrlFromSettings();
        }
        if (key.equals("username") || key.equals("password")) {
            updateAuthenticationFromSettings();
        }
    }

    private static <E, R extends RestEntityWrapper<E>> List<E> mapRestWrappers(List<R> wrappers) {
        List<E> entities = new ArrayList<>();
        for (R rest : wrappers) {
            entities.add(rest.toEntity());
        }
        return entities;
    }
}
