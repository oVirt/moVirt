package org.ovirt.mobile.movirt.ui.listfragment.spinner;

import org.ovirt.mobile.movirt.provider.SortOrder;
import org.springframework.util.StringUtils;

public class SortEntry {
    public static final SortEntry EMPTY = new SortEntry(null, null, null);

    private final ItemName itemName;
    private final SortOrderType sortOrder;

    private final SortOrder defaultSortOrder;

    private final String customSql;

    public SortEntry(ItemName itemName, SortOrderType sortOrder) {
        this(itemName, sortOrder, SortOrder.ASCENDING);
    }

    public SortEntry(ItemName itemName, SortOrderType sortOrder, SortOrder defaultSortOrder) {
        this(itemName, sortOrder, defaultSortOrder, null);
    }

    public SortEntry(ItemName itemName, SortOrderType sortOrder, SortOrder defaultSortOrder, String customSql) {
        this.itemName = itemName;
        this.sortOrder = sortOrder;
        this.defaultSortOrder = defaultSortOrder;
        this.customSql = customSql;
    }

    public SortOrderType getSortOrder() {
        return sortOrder;
    }

    public SortOrder getDefaultSortOrder() {
        return defaultSortOrder;
    }

    public ItemName getItemName() {
        return itemName;
    }

    public String orderBySql() {
        if (StringUtils.isEmpty(customSql)) {
            return itemName.getColumnName();
        }
        return customSql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SortEntry)) return false;

        SortEntry sortEntry = (SortEntry) o;

        if (itemName != null ? !itemName.equals(sortEntry.itemName) : sortEntry.itemName != null)
            return false;
        if (sortOrder != sortEntry.sortOrder) return false;
        if (defaultSortOrder != sortEntry.defaultSortOrder) return false;
        return customSql != null ? customSql.equals(sortEntry.customSql) : sortEntry.customSql == null;
    }

    @Override
    public int hashCode() {
        int result = itemName != null ? itemName.hashCode() : 0;
        result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
        result = 31 * result + (defaultSortOrder != null ? defaultSortOrder.hashCode() : 0);
        result = 31 * result + (customSql != null ? customSql.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return itemName.getDisplayName();
    }
}
