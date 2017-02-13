package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.ObjectUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotDisk extends org.ovirt.mobile.movirt.rest.dto.SnapshotDisk {
    public Status status;
    public String size;

    public org.ovirt.mobile.movirt.model.SnapshotDisk toEntity() {
        org.ovirt.mobile.movirt.model.SnapshotDisk snapshotDisk = super.toEntity();
        snapshotDisk.setSize(ObjectUtils.parseLong(size));

        if (status != null) {
            snapshotDisk.setStatus(status.state);
        }

        return snapshotDisk;
    }
}
