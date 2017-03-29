package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Host extends org.ovirt.mobile.movirt.rest.dto.Host {
    public Status status;
    public Cluster cluster;

    @Override
    public org.ovirt.mobile.movirt.model.Host toEntity() {
        org.ovirt.mobile.movirt.model.Host host = super.toEntity();
        host.setStatus(Status.asHostStatus(status));
        if (cluster != null) {
            host.setClusterId(cluster.id);
        }

        return host;
    }
}
