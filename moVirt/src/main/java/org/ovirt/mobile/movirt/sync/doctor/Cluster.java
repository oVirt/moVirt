package org.ovirt.mobile.movirt.sync.doctor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.sync.RestEntityWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cluster implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Cluster> {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @Override
    public org.ovirt.mobile.movirt.model.Cluster toEntity() {
        org.ovirt.mobile.movirt.model.Cluster cluster = new org.ovirt.mobile.movirt.model.Cluster();
        cluster.setId(id);
        cluster.setName(name);

        return cluster;
    }
}
