package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.rest.dto.common.Mac;
import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotNic implements RestEntityWrapper<org.ovirt.mobile.movirt.model.SnapshotNic>, HasId {
    public String id;
    public String name;
    public boolean linked;
    public Mac mac;
    public boolean plugged;

    public transient String vmId;
    public transient String snapshotId;

    @Override
    public String getId() {
        return id;
    }

    public org.ovirt.mobile.movirt.model.SnapshotNic toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.SnapshotNic nic = new org.ovirt.mobile.movirt.model.SnapshotNic();

        nic.setNicId(IdHelper.combinedId(accountId, id));
        nic.setSnapshotId(IdHelper.combinedId(accountId, snapshotId));
        nic.setVmId(IdHelper.combinedId(accountId, vmId));
        nic.setIds(accountId, IdHelper.combinedId(nic.getSnapshotId(), nic.getNicId())); // make unique id

        nic.setName(name);
        nic.setLinked(linked);
        if (mac != null) {
            nic.setMacAddress(mac.address);
        }
        nic.setPlugged(plugged);

        return nic;
    }
}
