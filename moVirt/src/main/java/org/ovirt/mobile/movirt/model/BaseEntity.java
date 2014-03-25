package org.ovirt.mobile.movirt.model;

import android.net.Uri;

public abstract class BaseEntity<ID> {
    public abstract ID getId();
    public abstract void setId(ID value);

    public abstract Uri getBaseUri();

    public Uri getUri() {
        return getBaseUri().buildUpon().appendPath(getId().toString()).build();
    }
}
