package org.ovirt.mobile.movirt.provider;

import com.blandware.android.atleap.provider.ormlite.OrmLiteProvider;
import com.blandware.android.atleap.provider.ormlite.OrmLiteUriMatcher;

public class OVirtProvider extends OrmLiteProvider<OVirtDatabaseHelper, OVirtUriMatcher> {
    public OVirtProvider() {
    }

    @Override
    protected OVirtDatabaseHelper createHelper() {
        return new OVirtDatabaseHelper(getContext());
    }

    @Override
    public OVirtUriMatcher getUriMatcher() {
        return OrmLiteUriMatcher.getInstance(OVirtUriMatcher.class, OVirtContract.CONTENT_AUTHORITY);
    }

    @Override
    public String getAuthority() {
        return OVirtContract.CONTENT_AUTHORITY;
    }
}
