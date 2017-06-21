package org.ovirt.mobile.movirt.provider;

public enum SortOrder {
    ASCENDING("ASC", 0),
    DESCENDING("DESC", 1);

    private final String order;
    private final int index;

    SortOrder(String order, int index) {
        this.order = order;
        this.index = index;
    }

    public int getIndex() {
        return index;
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

    public static SortOrder fromIndex(int index) {
        if (index == ASCENDING.index) {
            return ASCENDING;
        } else if (index == DESCENDING.index) {
            return DESCENDING;
        } else {
            return null;
        }
    }

    public static int toIndex(SortOrder sortOrder) {
        if (sortOrder == ASCENDING) {
            return ASCENDING.index;
        } else {
            return DESCENDING.index;
        }
    }
}
