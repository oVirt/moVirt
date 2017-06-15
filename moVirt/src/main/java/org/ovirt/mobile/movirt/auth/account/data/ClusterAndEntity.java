package org.ovirt.mobile.movirt.auth.account.data;

import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;

public class ClusterAndEntity<E extends OVirtEntity> {
    public final E entity;
    public final Cluster cluster;

    public ClusterAndEntity(E entity, Cluster cluster) {
        this.entity = entity;
        this.cluster = cluster;
    }

    public boolean hasCluster() {
        return cluster != null;
    }

    public String getClusterName() {
        return cluster == null ? null : cluster.getName();
    }
}
