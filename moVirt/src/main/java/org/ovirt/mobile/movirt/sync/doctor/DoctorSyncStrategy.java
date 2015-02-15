package org.ovirt.mobile.movirt.sync.doctor;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.sync.BaseSyncStrategy;
import org.ovirt.mobile.movirt.sync.OvirtSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.sync.Request;
import org.ovirt.mobile.movirt.sync.Response;
import org.ovirt.mobile.movirt.sync.rest.Disks;
import org.ovirt.mobile.movirt.sync.rest.Nics;

import java.util.List;

@EBean
public class DoctorSyncStrategy extends BaseSyncStrategy {

    @RestService
    DoctorRestClient restClient;

    @Bean
    OvirtSimpleClientHttpRequestFactory requestFactory;

    @Bean
    MovirtAuthenticator authenticator;

    @AfterInject
    void initClient() {
        restClient.setHeader("Accept-Encoding", "gzip");
        restClient.getRestTemplate().setRequestFactory(requestFactory);
    }

    public boolean isAvailable() {
        return authenticator.useDoctorRest();
    }

    @Override
    public void getVm(final String vmId, Response<Vm> response) {
        fireRestRequest(new Request<Vm>() {
            @Override
            public Vm fire() {
                return restClient.getVm(vmId).toEntity();
            }
        }, response);
    }

    @Override
    public void getVms(Response<List<Vm>> response) {
        fireRestRequest(new Request<List<Vm>>() {
            @Override
            public List<Vm> fire() {
                return mapRestWrappers(restClient.getVms(), null);
            }
        }, response);
    }

    @Override
    public void getHost(final String hostId, Response<Host> response) {
        fireRestRequest(new Request<Host>() {
            @Override
            public Host fire() {
                return restClient.getHost(hostId).toEntity();
            }
        }, response);
    }

    @Override
    public void getHosts(Response<List<Host>> response) {
        fireRestRequest(new Request<List<Host>>() {
            @Override
            public List<Host> fire() {
                return mapRestWrappers(restClient.getHosts(), null);
            }
        }, response);
    }

    @Override
    public void getDisks(String id, Response<Disks> response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getClusters(Response<List<Cluster>> response) {
        fireRestRequest(new Request<List<Cluster>>() {
            @Override
            public List<Cluster> fire() {
                return mapRestWrappers(restClient.getClusters(), null);
            }
        }, response);
    }

    @Override
    public void getNics(String id, Response<Nics> response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getEventsSince(int lastEventId, Response<List<Event>> response) {
        throw new UnsupportedOperationException();
    }

    private synchronized <T> void fireRestRequest(Request<T> request, Response<T> response) {
        if (response != null) {
            response.before();
        }

        try {
            restClient.setRootUrl(authenticator.getDoctorRestUrl());
            T result = request.fire();
            if (response != null) {
                response.onResponse(result);
            }
        } catch (Exception e) {
            if (response != null){
                fireConnectionError(e.getMessage());
                response.onError();
            }
        } finally {
            if (response != null) {
                response.after();
            }
        }
    }
}
