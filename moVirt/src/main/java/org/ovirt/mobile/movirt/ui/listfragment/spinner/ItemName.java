package org.ovirt.mobile.movirt.ui.listfragment.spinner;

import java.util.Arrays;
import java.util.Iterator;

public class ItemName {

    private String columnName;
    private String displayName;

    public ItemName(String columnName) {
        this.columnName = columnName;
        this.displayName = makeReadable(columnName);
    }

    public ItemName(String columnName, String displayName) {
        this.columnName = columnName;
        this.displayName = displayName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static String makeReadable(String string) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> parts = Arrays.asList(string.split("(_)|(\\s)")).iterator();

        while (parts.hasNext()) {
            builder.append(capitalize(parts.next()));
            if (parts.hasNext()) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
