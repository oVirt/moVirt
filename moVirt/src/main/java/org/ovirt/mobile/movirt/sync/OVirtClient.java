package org.ovirt.mobile.movirt.sync;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.sync.doctor.DoctorSyncStrategy;
import org.ovirt.mobile.movirt.sync.rest.ActionTicket;
import org.ovirt.mobile.movirt.sync.rest.Disks;
import org.ovirt.mobile.movirt.sync.rest.Nics;
import org.ovirt.mobile.movirt.sync.rest.OVirtRestSyncStrategy;

import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class OVirtClient implements SyncStrategy, ActionStrategy {
    private static final String TAG = OVirtClient.class.getSimpleName();

    @Bean
    MovirtAuthenticator authenticator;

    @Bean
    DoctorSyncStrategy doctorSyncStrategy;

    @Bean
    OVirtRestSyncStrategy oVirtRestSyncStrategy;

    @Override
    public void startVm(String vmId) {
        oVirtRestSyncStrategy.startVm(vmId);
    }

    @Override
    public void stopVm(String vmId) {
        oVirtRestSyncStrategy.stopVm(vmId);
    }

    @Override
    public void rebootVm(String vmId) {
        oVirtRestSyncStrategy.rebootVm(vmId);
    }

    @Override
    public void getConsoleTicket(String vmId, Response<ActionTicket> response) {
        oVirtRestSyncStrategy.getConsoleTicket(vmId, response);
    }

    @Override
    public String login(String apiUrl, String username, String password, boolean disableHttps, boolean hasAdminPrivileges) {
        return oVirtRestSyncStrategy.login(apiUrl, username, password, disableHttps, hasAdminPrivileges);
    }

    @Override
    public void getVm(String vmId, Response<Vm> response) {
        if (doctorSyncStrategy.isAvailable()) {
            doctorSyncStrategy.getVm(vmId, response);
        } else {
            oVirtRestSyncStrategy.getVm(vmId, response);
        }
    }

    @Override
    public void getVms(Response<List<Vm>> response) {
        if (doctorSyncStrategy.isAvailable()) {
            doctorSyncStrategy.getVms(response);
        } else {
            oVirtRestSyncStrategy.getVms(response);
        }
    }

    @Override
    public void getHost(String hostId, Response<Host> response) {
        if (doctorSyncStrategy.isAvailable()) {
            doctorSyncStrategy.getHost(hostId, response);
        } else {
            oVirtRestSyncStrategy.getHost(hostId, response);
        }
    }

    @Override
    public void getHosts(Response<List<Host>> response) {
        if (doctorSyncStrategy.isAvailable()) {
            doctorSyncStrategy.getHosts(response);
        } else {
            oVirtRestSyncStrategy.getHosts(response);
        }
    }

    @Override
    public void getClusters(Response<List<Cluster>> response) {
        if (doctorSyncStrategy.isAvailable()) {
            doctorSyncStrategy.getClusters(response);
        } else {
            oVirtRestSyncStrategy.getClusters(response);
        }
    }

    @Override
    public void getDisks(String id, Response<Disks> response) {
        oVirtRestSyncStrategy.getDisks(id, response);
    }

    @Override
    public void getNics(String id, Response<Nics> response) {
        oVirtRestSyncStrategy.getNics(id, response);
    }

    @Override
    public void getEventsSince(int lastEventId, Response<List<Event>> response) {
        oVirtRestSyncStrategy.getEventsSince(lastEventId, response);
    }
}
