package org.ovirt.mobile.movirt.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.blandware.android.atleap.provider.ormlite.OrmLiteDatabaseHelper;
import com.blandware.android.atleap.provider.ormlite.OrmLiteUriMatcher;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.ovirt.mobile.movirt.model.trigger.Trigger;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DatabaseHelper extends OrmLiteDatabaseHelper<UriMatcher> {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DB_NAME = "ovirt.db";

    private static final int SCHEMA_VERSION = 45; // see this::migrate

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
        // call instead of super.onUpgrade()
        migrate(connectionSource, oldVersion, newVersion);
        ViewHelper.replaceTablesWithViews(database);
    }

    @SuppressWarnings("unchecked")
    private void migrate(ConnectionSource connectionSource, int oldVersion, int newVersion) {
        boolean updateTriggers = true;

        // change for new migration rules
        if (45 <= oldVersion && newVersion < Integer.MAX_VALUE) {
            updateTriggers = false;
        }

        try {
            Set<Class<?>> classes = new HashSet<>(getUriMatcher().getClasses());
            for (Class clazz : classes) {
                if (!updateTriggers && Trigger.class.isAssignableFrom(clazz)) {
                    continue;
                }
                TableUtils.dropTable(connectionSource, clazz, true);
                TableUtils.createTable(connectionSource, clazz);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Cannot upgrade database", e);
        }
    }

    @Override
    public UriMatcher getUriMatcher() {
        return OrmLiteUriMatcher.getInstance(UriMatcher.class, OVirtContract.CONTENT_AUTHORITY);
    }
}
