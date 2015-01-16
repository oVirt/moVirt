package org.ovirt.mobile.movirt.provider;

public enum SortOrder {
    ASCENDING,
    DESCENDING;

    public static SortOrder from(String from) {
        if ("desc".equals(from.toLowerCase())) {
            return DESCENDING;
        } else {
            return ASCENDING;
        }
    }
}
