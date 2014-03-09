package org.ovirt.mobile.movirt.util;

import android.database.Cursor;

public class CursorHelper {

    private final Cursor cursor;

    public CursorHelper(Cursor cursor) {
        this.cursor = cursor;
    }

    public String getString(String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    public int getInt(String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }

    public <E extends Enum<E>> E getEnum(String columnName, Class<E> clazz) {
        return E.valueOf(clazz, getString(columnName));
    }

    public <T> T getJson(String columnName, Class<T> clazz) {
        return JsonUtils.stringToObject(getString(columnName), clazz);
    }
}
