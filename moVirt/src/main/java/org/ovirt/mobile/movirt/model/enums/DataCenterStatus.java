package org.ovirt.mobile.movirt.model.enums;

public enum DataCenterStatus {
    UNKNOWN(-1),
    CONTEND(0),
    MAINTENANCE(1),
    NOT_OPERATIONAL(2),
    PROBLEMATIC(3),
    UNINITIALIZED(4),
    UP(5);

    DataCenterStatus(int resource) {
        this.resource = resource;
    }

    private final int resource;

    public int getResource() {
        return resource;
    }

    public static DataCenterStatus fromString(String status) {
        try {
            return DataCenterStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return DataCenterStatus.UNKNOWN;
        }
    }
}
