package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.ParseUtils;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Cluster implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Cluster>, HasId {
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
    public org.ovirt.mobile.movirt.model.Cluster toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Cluster cluster = new org.ovirt.mobile.movirt.model.Cluster();
        cluster.setIds(accountId, id);
        cluster.setName(name);
        cluster.setVersion(ParseUtils.parseVersion(version));

        return cluster;
    }
}
