package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DataCenter implements RestEntityWrapper<org.ovirt.mobile.movirt.model.DataCenter> {
    public String id;
    public String name;
    public Version version;

    @Override
    public org.ovirt.mobile.movirt.model.DataCenter toEntity() {
        org.ovirt.mobile.movirt.model.DataCenter dataCenter = new org.ovirt.mobile.movirt.model.DataCenter();
        dataCenter.setId(id);
        dataCenter.setName(name);
        dataCenter.setVersion(ParseUtils.parseVersion(version));

        return dataCenter;
    }
}
