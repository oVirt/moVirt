package org.ovirt.mobile.movirt.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.blandware.android.atleap.provider.ormlite.OrmLiteDatabaseHelper;
import com.blandware.android.atleap.provider.ormlite.OrmLiteUriMatcher;
import com.j256.ormlite.support.ConnectionSource;

public class DatabaseHelper extends OrmLiteDatabaseHelper<UriMatcher> {

    private static final String DB_NAME = "ovirt.db";

    private static final int SCHEMA_VERSION = 45;

    private volatile static DatabaseHelper instance = null;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, SCHEMA_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        super.onCreate(database, connectionSource);
        ViewHelper.replaceTablesWithViews(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        ViewHelper.dropViews(database);
        super.onUpgrade(database, connectionSource, oldVersion, newVersion);
        ViewHelper.replaceTablesWithViews(database);
    }

    @Override
    public UriMatcher getUriMatcher() {
        return OrmLiteUriMatcher.getInstance(UriMatcher.class, OVirtContract.CONTENT_AUTHORITY);
    }
}
