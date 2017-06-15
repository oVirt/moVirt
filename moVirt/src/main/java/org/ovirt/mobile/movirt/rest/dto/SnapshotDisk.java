package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SnapshotDisk implements RestEntityWrapper<org.ovirt.mobile.movirt.model.SnapshotDisk>, HasId {
    public String id;
    public String name;
    public String actual_size;

    public transient String vmId;
    public transient String snapshotId;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public org.ovirt.mobile.movirt.model.SnapshotDisk toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.SnapshotDisk snapshotDisk = new org.ovirt.mobile.movirt.model.SnapshotDisk();

        snapshotDisk.setDiskId(IdHelper.combinedId(accountId, id));
        snapshotDisk.setSnapshotId(IdHelper.combinedId(accountId, snapshotId));
        snapshotDisk.setVmId(IdHelper.combinedId(accountId, vmId));
        snapshotDisk.setIds(accountId,
                IdHelper.combinedId(snapshotDisk.getSnapshotId(), snapshotDisk.getDiskId())); // make unique id

        snapshotDisk.setName(name);
        snapshotDisk.setUsedSize(ObjectUtils.parseLong(actual_size));

        return snapshotDisk;
    }
}
