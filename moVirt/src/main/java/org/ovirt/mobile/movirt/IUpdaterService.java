package org.ovirt.mobile.movirt;


import org.ovirt.mobile.movirt.rest.Cluster;
import org.ovirt.mobile.movirt.rest.Vm;

import java.util.List;

public interface IUpdaterService {
    void fullUpdate();

    List<Vm> getCachedVms();
    List<Cluster> getCachedClusters();
}
