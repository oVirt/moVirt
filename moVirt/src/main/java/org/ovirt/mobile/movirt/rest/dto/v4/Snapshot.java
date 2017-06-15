package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.SnapshotType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Snapshot extends org.ovirt.mobile.movirt.rest.dto.Snapshot {
    public SnapshotVm vm;
    public String snapshot_type;

    public Snapshot() {
    }

    public Snapshot(String description, boolean persistMemoryState) {
        super(description, persistMemoryState);
    }

    public org.ovirt.mobile.movirt.model.Snapshot toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Snapshot snapshot = super.toEntity(accountId);
        snapshot.setType(SnapshotType.fromString(snapshot_type));

        if (vm != null) {
            vm.snapshotId = snapshot.getId();
            snapshot.setVm(vm.toEntity(accountId));
        }

        return snapshot;
    }
}
