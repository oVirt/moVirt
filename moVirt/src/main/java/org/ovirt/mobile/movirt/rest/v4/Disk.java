package org.ovirt.mobile.movirt.rest.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.ObjectUtils;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Disk extends org.ovirt.mobile.movirt.rest.Disk {
    public String status;
    public Vm vm;
    public String provisioned_size;
    public Snapshot snapshot;

    public org.ovirt.mobile.movirt.model.Disk toEntity() {
        org.ovirt.mobile.movirt.model.Disk disk = super.toEntity();
        disk.setStatus(status);
        disk.setSize(ObjectUtils.parseLong(provisioned_size));

        if (vm != null) {
            disk.setVmId(vm.id);
        }

        if (snapshot != null) {
            disk.setSnapshotId(snapshot.id);
        }

        return disk;
    }
}
