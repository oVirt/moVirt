package org.ovirt.mobile.movirt.sync.doctor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.sync.RestEntityWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Host implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Host> {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("status/state")
    public String status;

    @JsonProperty("_links")
    public Links links;

    @Override
    public org.ovirt.mobile.movirt.model.Host toEntity() {
        org.ovirt.mobile.movirt.model.Host host = new org.ovirt.mobile.movirt.model.Host();
        host.setId(id);
        host.setName(name);
        host.setStatus(org.ovirt.mobile.movirt.model.Host.Status.valueOf(status.toUpperCase()));
        host.setClusterId(links.cluster);
        return host;
    }
}
