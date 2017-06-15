package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.ObjectUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotDisk extends org.ovirt.mobile.movirt.rest.dto.SnapshotDisk {
    public String status;
    public String provisioned_size;

    public org.ovirt.mobile.movirt.model.SnapshotDisk toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.SnapshotDisk snapshotDisk = super.toEntity(accountId);
        snapshotDisk.setStatus(status);
        snapshotDisk.setSize(ObjectUtils.parseLong(provisioned_size));

        return snapshotDisk;
    }
}
