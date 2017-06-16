package org.ovirt.mobile.movirt.ui.listfragment.spinner;

import org.ovirt.mobile.movirt.provider.SortOrder;

import java.util.HashMap;

public enum SortOrderType {
    A_TO_Z("A to Z", "Z to A"),
    LOW_TO_HIGH("Low to High", "High to Low"),
    OLDEST_TO_LATEST("Oldest", "Latest");

    private String ascDisplayName;
    private String descDisplayName;

    private static HashMap<String, SortOrderType> descMap = new HashMap<>(values().length);

    static {
        for (SortOrderType o : values()) {
            if (descMap.put(o.descDisplayName, o) != null) {
                throw new IllegalStateException("Invalid mapping");
            }
        }
    }

    SortOrderType(String ascDisplayName, String descDisplayName) {
        this.ascDisplayName = ascDisplayName;
        this.descDisplayName = descDisplayName;
    }

    public String getAscDisplayName() {
        return ascDisplayName;
    }

    public String getDescDisplayName() {
        return descDisplayName;
    }

    public String getDisplayNameBySortOrder(SortOrder sortOrder) {
        return sortOrder == SortOrder.ASCENDING ? ascDisplayName : descDisplayName;
    }

    public static org.ovirt.mobile.movirt.provider.SortOrder getSortOrder(String displayName) {
        if (descMap.get(displayName) != null) {
            return org.ovirt.mobile.movirt.provider.SortOrder.DESCENDING;
        }
        return org.ovirt.mobile.movirt.provider.SortOrder.ASCENDING;
    }
}
