package org.ovirt.mobile.movirt.provider;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ViewHelper {
    private static final String TAG = ViewHelper.class.getSimpleName();

    static void replaceTablesWithViews(SQLiteDatabase database) {
        database.beginTransaction();
        try {
            dropMappedTables(database);
            createAll(database);

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    static void dropViews(SQLiteDatabase database) {
        database.beginTransaction();
        try {
            dropAll(database);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * drop tables to be substituted by views
     */
    private static void dropMappedTables(SQLiteDatabase database) {
        for (View view : Views.getViews()) {
            Log.i(TAG, "dropping table " + view.table);
            database.execSQL(String.format("DROP TABLE IF EXISTS %s", view.table));
        }
    }

    private static void dropAll(SQLiteDatabase database) {
        for (View view : Views.getViews()) {
            Log.i(TAG, "dropping view " + view.table);
            database.execSQL(String.format("DROP VIEW IF EXISTS %s", view.table));
        }
    }

    private static void createAll(SQLiteDatabase database) {
        for (View view : Views.getViews()) {
            Log.i(TAG, "creating view " + view.table);
            database.execSQL(view.sql);
        }
    }

    public static class View {
        private String table;
        private String sql;

        public View(String table, String sql) {
            this.table = table;
            this.sql = sql;
        }
    }
}
