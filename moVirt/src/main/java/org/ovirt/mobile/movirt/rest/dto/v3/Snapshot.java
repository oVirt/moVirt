package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.SnapshotType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Snapshot extends org.ovirt.mobile.movirt.rest.dto.Snapshot {
    public SnapshotVm vm;
    public String type;

    public Snapshot() {
    }

    public Snapshot(String description, boolean persistMemoryState) {
        super(description, persistMemoryState);
    }

    public org.ovirt.mobile.movirt.model.Snapshot toEntity() {
        org.ovirt.mobile.movirt.model.Snapshot snapshot = super.toEntity();
        snapshot.setType(SnapshotType.fromString(type));

        if (vm != null) {
            vm.snapshotId = id;
            snapshot.setVm(vm.toEntity());
        }

        return snapshot;
    }
}
