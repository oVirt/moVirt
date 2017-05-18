package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.SnapshotStatus;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Snapshot implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Snapshot>, HasId {

    public String id;
    public String description;
    public String snapshot_status;
    public long date;
    public boolean persist_memorystate;

    public transient String vmId;

    public Snapshot() {
    }

    @Override
    public String getId() {
        return id;
    }

    public Snapshot(String description, boolean persistMemoryState) {
        this.description = description;
        this.persist_memorystate = persistMemoryState;
    }

    public org.ovirt.mobile.movirt.model.Snapshot toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Snapshot snapshot = new org.ovirt.mobile.movirt.model.Snapshot();
        snapshot.setIds(accountId, id);
        snapshot.setVmId(IdHelper.combinedIdSafe(accountId, vmId));
        snapshot.setName(description);
        snapshot.setSnapshotStatus(SnapshotStatus.fromString(snapshot_status));

        snapshot.setDate(date);
        snapshot.setPersistMemorystate(persist_memorystate);

        return snapshot;
    }
}
