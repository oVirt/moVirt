package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.Snapshot.SnapshotStatus;
import org.ovirt.mobile.movirt.model.Snapshot.SnapshotType;

/**
 * Created by suomiy on 11/25/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Snapshot implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Snapshot> {
    public String id;
    public String description;
    public String snapshot_status;
    public long date;
    public boolean persist_memorystate;

    public Snapshot() {
    }

    public Snapshot(String description, boolean persistMemoryState) {
        this.description = description;
        this.persist_memorystate = persistMemoryState;
    }

    @Override
    public String toString() {
        return String.format("Snapshot: id=%id, description=%s, snapshot_status=%s, date=%d, persist_memorystate=%b",
                id, description, snapshot_status, date, persist_memorystate);
    }

    public org.ovirt.mobile.movirt.model.Snapshot toEntity() {
        org.ovirt.mobile.movirt.model.Snapshot snapshot = new org.ovirt.mobile.movirt.model.Snapshot();
        snapshot.setId(id);
        snapshot.setName(description);
        if (snapshot_status != null) {
            snapshot.setSnapshotStatus(SnapshotStatus.valueOf(snapshot_status.toUpperCase()));
        }

        snapshot.setDate(date);
        snapshot.setPersistMemorystate(persist_memorystate);

        return snapshot;
    }

    protected org.ovirt.mobile.movirt.model.Snapshot.SnapshotType getSnapshotType(String type) {
        try {
            return org.ovirt.mobile.movirt.model.Snapshot.SnapshotType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.Snapshot.SnapshotType.UNKNOWN;
        }
    }
}
