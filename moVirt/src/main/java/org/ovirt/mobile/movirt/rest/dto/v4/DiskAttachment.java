package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiskAttachment extends org.ovirt.mobile.movirt.rest.dto.DiskAttachment {
    public Disk disk;
    public Vm vm;

    public org.ovirt.mobile.movirt.model.DiskAttachment toEntity() {
        org.ovirt.mobile.movirt.model.DiskAttachment diskAttachment = super.toEntity();

        if (vm != null && disk != null) {
            diskAttachment.setId(vm.id + disk.id);
            diskAttachment.setVmId(vm.id);
            diskAttachment.setDiskId(disk.id);
        }

        return diskAttachment;
    }

    public static org.ovirt.mobile.movirt.model.DiskAttachment fromV3Disk(org.ovirt.mobile.movirt.model.Disk disk) {
        org.ovirt.mobile.movirt.model.DiskAttachment diskAttachment = new org.ovirt.mobile.movirt.model.DiskAttachment();

        if (disk.getVmId() != null && disk.getId() != null) {
            diskAttachment.setId(disk.getVmId() + disk.getId());
            diskAttachment.setVmId(disk.getVmId());
            diskAttachment.setDiskId(disk.getId());
        }

        return diskAttachment;
    }
}
