package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiskAttachment extends org.ovirt.mobile.movirt.rest.dto.DiskAttachment {
    public Disk disk;
    public Vm vm;

    public org.ovirt.mobile.movirt.model.DiskAttachment toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.DiskAttachment diskAttachment = super.toEntity(accountId);

        diskAttachment.setVmId(IdHelper.combinedId(accountId, vm));
        diskAttachment.setDiskId(IdHelper.combinedId(accountId, disk));
        diskAttachment.setIds(accountId,
                IdHelper.combinedId(diskAttachment.getVmId(), diskAttachment.getDiskId()));

        return diskAttachment;
    }

    public static org.ovirt.mobile.movirt.model.DiskAttachment fromV3Disk(org.ovirt.mobile.movirt.model.Disk disk) {
        org.ovirt.mobile.movirt.model.DiskAttachment diskAttachment = new org.ovirt.mobile.movirt.model.DiskAttachment();

        if (disk == null || disk.getVmId() == null || disk.getId() == null) {
            throw new IllegalArgumentException("cannot create composite id");
        }

        diskAttachment.setVmId(disk.getVmId());
        diskAttachment.setDiskId(disk.getId());
        diskAttachment.setIds(disk.getAccountId(),
                IdHelper.combinedId(diskAttachment.getVmId(), diskAttachment.getDiskId()));

        return diskAttachment;
    }
}
