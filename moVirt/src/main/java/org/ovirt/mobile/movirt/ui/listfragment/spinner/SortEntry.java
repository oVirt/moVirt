package org.ovirt.mobile.movirt.ui.listfragment.spinner;

import org.ovirt.mobile.movirt.provider.SortOrder;
import org.springframework.util.StringUtils;

public class SortEntry {

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
    public String toString() {
        return itemName.getDisplayName();
    }
}
