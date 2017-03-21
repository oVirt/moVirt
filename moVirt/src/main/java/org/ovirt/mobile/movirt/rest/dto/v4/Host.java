package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.HostStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Host extends org.ovirt.mobile.movirt.rest.dto.Host {
    public String status;
    public Cluster cluster;

    @Override
    public org.ovirt.mobile.movirt.model.Host toEntity() {
        org.ovirt.mobile.movirt.model.Host host = super.toEntity();
        host.setStatus(HostStatus.fromString(status));
        if (cluster != null) {
            host.setClusterId(cluster.id);
        }

        return host;
    }
}
