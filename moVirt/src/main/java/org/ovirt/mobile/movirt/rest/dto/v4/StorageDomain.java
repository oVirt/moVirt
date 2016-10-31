package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageDomain extends org.ovirt.mobile.movirt.rest.dto.StorageDomain {
    public String status;

    @Override
    public org.ovirt.mobile.movirt.model.StorageDomain toEntity() {
        org.ovirt.mobile.movirt.model.StorageDomain storageDomain = super.toEntity();

        if (status != null) {
            storageDomain.setStatus(mapStatus(status));
        } else {
            storageDomain.setStatus(org.ovirt.mobile.movirt.model.StorageDomain.Status.ACTIVE);
        }

        return storageDomain;
    }

    private static org.ovirt.mobile.movirt.model.StorageDomain.Status mapStatus(String status) {
        try {
            return org.ovirt.mobile.movirt.model.StorageDomain.Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.StorageDomain.Status.UNKNOWN;
        }
    }

}
