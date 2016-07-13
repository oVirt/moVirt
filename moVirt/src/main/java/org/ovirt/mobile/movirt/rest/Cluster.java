package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Cluster implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Cluster> {
    // public for json mapping
    public String id;
    public String name;
    public Version version;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public org.ovirt.mobile.movirt.model.Cluster toEntity() {
        org.ovirt.mobile.movirt.model.Cluster cluster = new org.ovirt.mobile.movirt.model.Cluster();
        cluster.setId(id);
        cluster.setName(name);
        cluster.setVersion(ParseUtils.parseVersion(version));

        return cluster;
    }
}
