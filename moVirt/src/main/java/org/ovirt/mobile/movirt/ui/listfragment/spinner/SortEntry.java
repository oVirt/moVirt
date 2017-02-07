package org.ovirt.mobile.movirt.ui.listfragment.spinner;

public class SortEntry {

    private ItemName itemName;
    private SortOrderType sortOrder;

    public SortEntry(ItemName itemName, SortOrderType sortOrder) {
        this.itemName = itemName;
        this.sortOrder = sortOrder;
    }

    public SortOrderType getSortOrder() {
        return sortOrder;
    }

    public ItemName getItemName() {
        return itemName;
    }

    @Override
    public String toString() {
        return itemName.getDisplayName();
    }
}
