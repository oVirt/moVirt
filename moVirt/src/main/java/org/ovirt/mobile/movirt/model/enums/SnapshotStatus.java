package org.ovirt.mobile.movirt.model.enums;

public enum SnapshotStatus {
    OK,
    LOCKED,
    IN_PREVIEW,
    UNKNOWN; // for failed parse only

    public static SnapshotStatus fromString(String status) {
        try {
            return SnapshotStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return SnapshotStatus.UNKNOWN;
        }
    }
}
