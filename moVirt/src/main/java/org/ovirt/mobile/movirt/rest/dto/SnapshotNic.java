package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.Mac;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotNic implements RestEntityWrapper<org.ovirt.mobile.movirt.model.SnapshotNic> {
    public String id;
    public String name;
    public boolean linked;
    public Mac mac;
    public boolean plugged;

    public transient String vmId;
    public transient String snapshotId;

    public org.ovirt.mobile.movirt.model.SnapshotNic toEntity() {
        org.ovirt.mobile.movirt.model.SnapshotNic nic = new org.ovirt.mobile.movirt.model.SnapshotNic();

        if (snapshotId == null || id == null) {
            throw new IllegalArgumentException("cannot create composite id");
        }

        nic.setId(id + snapshotId); // make unique id
        nic.setNicId(id);
        nic.setSnapshotId(snapshotId);
        nic.setVmId(vmId);

        nic.setName(name);
        nic.setLinked(linked);
        if (mac != null) {
            nic.setMacAddress(mac.address);
        }
        nic.setPlugged(plugged);

        return nic;
    }
}
