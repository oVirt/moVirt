package org.ovirt.mobile.movirt.model.base;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

public abstract class BaseEntity<ID> implements OVirtContract.BaseEntity {
    public abstract ID getId();

    public abstract void setId(ID value);

    public abstract Uri getBaseUri();

    public Uri getUri() {
        return getBaseUri().buildUpon().appendPath(getId().toString()).build();
    }

    public abstract ContentValues toValues();

    public final void initFromCursor(Cursor cursor) {
        initFromCursorHelper(new CursorHelper(cursor));
    }

    protected abstract void initFromCursorHelper(CursorHelper cursorHelper);
}
