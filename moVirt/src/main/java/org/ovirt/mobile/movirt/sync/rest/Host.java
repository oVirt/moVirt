package org.ovirt.mobile.movirt.sync.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.sync.RestEntityWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
class Host implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Host> {

    // public for json mapping
    public String id;
    public String name;
    public Status status;
    public Cluster cluster;

    @Override
    public org.ovirt.mobile.movirt.model.Host toEntity() {
        org.ovirt.mobile.movirt.model.Host host = new org.ovirt.mobile.movirt.model.Host();
        host.setId(id);
        host.setName(name);
        host.setStatus(mapStatus(status.state));
        host.setClusterId(cluster.id);

        return host;
    }

    private static org.ovirt.mobile.movirt.model.Host.Status mapStatus(String state) {
        return org.ovirt.mobile.movirt.model.Host.Status.valueOf(state.toUpperCase());
    }
}
