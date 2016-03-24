package org.ovirt.mobile.movirt.provider;

public enum SortOrder {
    ASCENDING("ASC"),
    DESCENDING("DESC");

    private final String order;

    SortOrder(String order) {
        this.order = order;
    }

    public boolean equalsOrder(String order) {
        return (order != null) && this.order.equals(order);
    }

    public String toString() {
        return this.order;
    }

    public static SortOrder from(String from) {
        if ("desc".equals(from.toLowerCase())) {
            return DESCENDING;
        } else {
            return ASCENDING;
        }
    }
}
