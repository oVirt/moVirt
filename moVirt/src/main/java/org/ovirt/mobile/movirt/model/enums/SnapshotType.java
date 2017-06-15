package org.ovirt.mobile.movirt.model.enums;

public enum SnapshotType {
    REGULAR,
    ACTIVE,
    STATELESS,
    PREVIEW,
    UNKNOWN;

    public static SnapshotType fromString(String type) {
        try {
            return SnapshotType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return SnapshotType.UNKNOWN;
        }
    }
}
