package org.ovirt.mobile.movirt.rest.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCenter extends org.ovirt.mobile.movirt.rest.DataCenter {
    public Status status;

    @Override
    public org.ovirt.mobile.movirt.model.DataCenter toEntity() {
        org.ovirt.mobile.movirt.model.DataCenter dataCenter = super.toEntity();
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
