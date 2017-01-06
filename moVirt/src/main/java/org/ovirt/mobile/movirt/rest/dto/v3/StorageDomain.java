package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageDomain extends org.ovirt.mobile.movirt.rest.dto.StorageDomain {
    public Status status;

    @Override
    public org.ovirt.mobile.movirt.model.StorageDomain toEntity() {
        org.ovirt.mobile.movirt.model.StorageDomain storageDomain = super.toEntity();

        storageDomain.setStatus(mapStatus(status));

        return storageDomain;
    }

    private org.ovirt.mobile.movirt.model.StorageDomain.Status mapStatus(Status status) {
        return super.mapStatus(status == null ? null : status.state);
    }
}
