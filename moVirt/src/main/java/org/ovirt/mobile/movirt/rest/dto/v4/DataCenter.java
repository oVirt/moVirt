package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.DataCenterStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCenter extends org.ovirt.mobile.movirt.rest.dto.DataCenter {
    public String status;

    @Override
    public org.ovirt.mobile.movirt.model.DataCenter toEntity() {
        org.ovirt.mobile.movirt.model.DataCenter dataCenter = super.toEntity();
        dataCenter.setStatus(DataCenterStatus.fromString(status));

        return dataCenter;
    }
}
