package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCenter extends org.ovirt.mobile.movirt.rest.dto.DataCenter {
    public Status status;

    @Override
    public org.ovirt.mobile.movirt.model.DataCenter toEntity() {
        org.ovirt.mobile.movirt.model.DataCenter dataCenter = super.toEntity();
        dataCenter.setStatus(Status.asDataCenterStatus(status));

        return dataCenter;
    }
}
