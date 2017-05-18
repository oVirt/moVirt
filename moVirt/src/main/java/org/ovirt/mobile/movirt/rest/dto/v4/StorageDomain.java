package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.StorageDomainStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageDomain extends org.ovirt.mobile.movirt.rest.dto.StorageDomain {
    public String status;

    @Override
    public org.ovirt.mobile.movirt.model.StorageDomain toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.StorageDomain storageDomain = super.toEntity(accountId);

        storageDomain.setStatus(StorageDomainStatus.fromString(status));

        return storageDomain;
    }
}
