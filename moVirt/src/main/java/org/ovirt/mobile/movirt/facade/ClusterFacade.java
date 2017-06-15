package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.rest.Request;

import java.util.List;

@EBean
public class ClusterFacade extends BaseEntityFacade<Cluster> {

    public ClusterFacade() {
        super(Cluster.class);
    }

    @Override
    protected Request<List<Cluster>> getSyncAllRestRequest(String... ids) {
        return oVirtClient.getClustersRequest();
    }
}
