package org.ovirt.mobile.movirt.provider;

import android.content.Context;

import com.blandware.android.atleap.provider.ormlite.OrmLiteDatabaseHelper;
import com.blandware.android.atleap.provider.ormlite.OrmLiteUriMatcher;

public class DatabaseHelper extends OrmLiteDatabaseHelper<UriMatcher> {

    private static final String DB_NAME = "ovirt.db";

    private static final int SCHEMA_VERSION = 32;

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, SCHEMA_VERSION);
    }

    @Override
    public UriMatcher getUriMatcher() {
        return OrmLiteUriMatcher.getInstance(UriMatcher.class, OVirtContract.CONTENT_AUTHORITY);
    }

}
