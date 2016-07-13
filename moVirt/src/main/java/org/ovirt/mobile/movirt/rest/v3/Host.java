package org.ovirt.mobile.movirt.rest.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Host extends org.ovirt.mobile.movirt.rest.Host {
    public Status status;
    public Cluster cluster;

    @Override
    public org.ovirt.mobile.movirt.model.Host toEntity() {
        org.ovirt.mobile.movirt.model.Host host = super.toEntity();
        host.setStatus(mapStatus(status));
        if (cluster != null) {
            host.setClusterId(cluster.id);
        }

        return host;
    }

    private static org.ovirt.mobile.movirt.model.Host.Status mapStatus(Status state) {
        try {
            return org.ovirt.mobile.movirt.model.Host.Status.valueOf(state.state.toUpperCase());
        } catch (Exception e) {
            // this is the error status also on engine
            return org.ovirt.mobile.movirt.model.Host.Status.UNASSIGNED;
        }
    }
}
