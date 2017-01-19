package org.ovirt.mobile.movirt.ui.listfragment.spinner;

import org.ovirt.mobile.movirt.provider.SortOrder;

public class CustomSort {

    private CustomSortEntry[] sortEntries;

    public CustomSort(CustomSortEntry[] sortEntries) {
        this.sortEntries = sortEntries;
    }

    public CustomSortEntry[] getSortEntries() {
        return sortEntries;
    }

    public static class CustomSortEntry {

        private String columnName;
        private org.ovirt.mobile.movirt.provider.SortOrder sortOrder;

        public CustomSortEntry(String columnName, SortOrder sortOrder) {
            this.columnName = columnName;
            this.sortOrder = sortOrder;
        }

        public String getColumnName() {
            return columnName;
        }

        public SortOrder getSortOrder() {
            return sortOrder;
        }
    }
}
