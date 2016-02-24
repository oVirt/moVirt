package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by suomiy on 2/17/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we don't use snapshot when POSTing restore
public class SnapshotAction {
    public Snapshot snapshot;
    public boolean restore_memory;

    public SnapshotAction(boolean restoreMemory) {
        this.restore_memory = restoreMemory;
    }

    public SnapshotAction(String snapshotId, boolean restoreMemory) {
        this.snapshot = new Snapshot(snapshotId);
        this.restore_memory = restoreMemory;
    }

    public static class Snapshot {
        public String id;

        public Snapshot(String id) {
            this.id = id;
        }
    }
}
