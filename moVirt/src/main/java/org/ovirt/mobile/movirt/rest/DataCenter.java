package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCenter implements RestEntityWrapper<org.ovirt.mobile.movirt.model.DataCenter>{
    public String id;
    public String name;
    public Version version;
    public Status status;

    @Override
    public org.ovirt.mobile.movirt.model.DataCenter toEntity() {
        org.ovirt.mobile.movirt.model.DataCenter dataCenter = new org.ovirt.mobile.movirt.model.DataCenter();
        dataCenter.setId(id);
        dataCenter.setName(name);
        dataCenter.setVersion(ParseUtils.parseVersion(version));
        dataCenter.setStatus(mapStatus(status));

        return dataCenter;
    }

    private static org.ovirt.mobile.movirt.model.DataCenter.Status mapStatus(Status status) {
        try {
            return org.ovirt.mobile.movirt.model.DataCenter.Status.valueOf(status.state.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.DataCenter.Status.UNKNOWN;
        }
    }
}
