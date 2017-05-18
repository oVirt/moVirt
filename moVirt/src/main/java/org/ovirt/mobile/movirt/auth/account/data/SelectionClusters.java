package org.ovirt.mobile.movirt.auth.account.data;

import org.ovirt.mobile.movirt.model.Cluster;

import java.util.Collection;

public class SelectionClusters {

    private Collection<Cluster> clusters;
    private ActiveSelection activeSelection;

    public SelectionClusters(Collection<Cluster> clusters, ActiveSelection activeSelection) {
        this.clusters = clusters;
        this.activeSelection = activeSelection;
    }

    public Collection<Cluster> getClusters() {
        return clusters;
    }

    public ActiveSelection getActiveSelection() {
        return activeSelection;
    }
}
