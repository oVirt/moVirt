package org.ovirt.mobile.movirt.provider;

import com.blandware.android.atleap.provider.ormlite.OrmLiteProvider;
import com.blandware.android.atleap.provider.ormlite.OrmLiteUriMatcher;

public class Provider extends OrmLiteProvider<DatabaseHelper, UriMatcher> {
    public Provider() {
    }

    @Override
    protected DatabaseHelper createHelper() {
        return DatabaseHelper.getInstance(getContext());
    }

    @Override
    public UriMatcher getUriMatcher() {
        return OrmLiteUriMatcher.getInstance(UriMatcher.class, OVirtContract.CONTENT_AUTHORITY);
    }

    @Override
    public String getAuthority() {
        return OVirtContract.CONTENT_AUTHORITY;
    }
}
