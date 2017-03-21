package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SnapshotDisk implements RestEntityWrapper<org.ovirt.mobile.movirt.model.SnapshotDisk> {
    public String id;
    public String name;
    public String actual_size;

    public transient String vmId;
    public transient String snapshotId;

    public org.ovirt.mobile.movirt.model.SnapshotDisk toEntity() {
        org.ovirt.mobile.movirt.model.SnapshotDisk snapshotDisk = new org.ovirt.mobile.movirt.model.SnapshotDisk();

        if (snapshotId == null || id == null) {
            throw new IllegalArgumentException("cannot create composite id");
        }

        snapshotDisk.setId(id + snapshotId); // make unique id
        snapshotDisk.setDiskId(id);
        snapshotDisk.setSnapshotId(snapshotId);
        snapshotDisk.setVmId(vmId);

        snapshotDisk.setName(name);
        snapshotDisk.setUsedSize(ObjectUtils.parseLong(actual_size));

        return snapshotDisk;
    }
}
