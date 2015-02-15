package org.ovirt.mobile.movirt.sync;

import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.sync.rest.Disks;
import org.ovirt.mobile.movirt.sync.rest.Nics;

import java.util.List;

public interface SyncStrategy {
    void getVm(String vmId, Response<Vm> response);

    void getVms(Response<List<Vm>> response);

    void getHost(String hostId, Response<Host> response);

    void getHosts(Response<List<Host>> response);

    void getClusters(Response<List<Cluster>> response);

    void getDisks(String id, Response<Disks> response);

    void getNics(String id, Response<Nics> response);

    void getEventsSince(int lastEventId, Response<List<Event>> response);
}
