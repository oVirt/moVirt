package org.ovirt.mobile.movirt.provider;

import android.content.Context;

import com.blandware.android.atleap.provider.ormlite.OrmLiteDatabaseHelper;
import com.blandware.android.atleap.provider.ormlite.OrmLiteUriMatcher;

public class OVirtDatabaseHelper extends OrmLiteDatabaseHelper<OVirtUriMatcher> {

    private static final String DB_NAME = "ovirt.db";
    private static final int SCHEMA_VERSION = 1;
    private static final String TAG = OVirtDatabaseHelper.class.getSimpleName();

    public OVirtDatabaseHelper(Context context) {
        super(context, DB_NAME, SCHEMA_VERSION);
    }

    @Override
    public OVirtUriMatcher getUriMatcher() {
        return OrmLiteUriMatcher.getInstance(OVirtUriMatcher.class, OVirtContract.CONTENT_AUTHORITY);
    }

//    @Override
//    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
//        try {
//            TableUtils.createTableIfNotExists(connectionSource, Vm.class);
//        } catch (SQLException e) {
//            Log.e(TAG, e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
//        try {
//            TableUtils.dropTable(connectionSource, Vm.class, true);
//            TableUtils.createTable(connectionSource, Vm.class);
//        } catch (SQLException e) {
//            Log.e(TAG, e.getMessage(), e);
//        }
//
//    }

}
