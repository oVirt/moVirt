package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import org.ovirt.mobile.movirt.provider.OVirtContract;

public abstract class BaseEntity<ID> implements OVirtContract.BaseEntity {
    public abstract ID getId();
    public abstract void setId(ID value);

    public abstract Uri getBaseUri();

    public Uri getUri() {
        return getBaseUri().buildUpon().appendPath(getId().toString()).build();
    }

    public abstract ContentValues toValues();
}
