package org.ovirt.mobile.movirt.rest.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageDomain extends org.ovirt.mobile.movirt.rest.StorageDomain {
    public Status status;

    @Override
    public org.ovirt.mobile.movirt.model.StorageDomain toEntity() {
        org.ovirt.mobile.movirt.model.StorageDomain storageDomain = super.toEntity();

        if (status != null && status.state != null) {
            storageDomain.setStatus(mapStatus(status));
        } else {
            storageDomain.setStatus(org.ovirt.mobile.movirt.model.StorageDomain.Status.ACTIVE);
        }

        return storageDomain;
    }

    private static org.ovirt.mobile.movirt.model.StorageDomain.Status mapStatus(Status status) {
        try {
            return org.ovirt.mobile.movirt.model.StorageDomain.Status.valueOf(status.state.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.StorageDomain.Status.UNKNOWN;
        }
    }
}
