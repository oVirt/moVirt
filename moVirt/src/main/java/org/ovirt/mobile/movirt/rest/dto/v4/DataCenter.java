package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCenter extends org.ovirt.mobile.movirt.rest.dto.DataCenter {
    public String status;

    @Override
    public org.ovirt.mobile.movirt.model.DataCenter toEntity() {
        org.ovirt.mobile.movirt.model.DataCenter dataCenter = super.toEntity();
        dataCenter.setStatus(mapStatus(status));

        return dataCenter;
    }

    private static org.ovirt.mobile.movirt.model.DataCenter.Status mapStatus(String status) {
        try {
            return org.ovirt.mobile.movirt.model.DataCenter.Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.DataCenter.Status.UNKNOWN;
        }
    }
}
