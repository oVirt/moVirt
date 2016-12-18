package org.ovirt.mobile.movirt.model.base;

import android.content.ContentValues;
import android.net.Uri;

public abstract class OvirtViewEntity extends OVirtEntity {

    @Override
    public Uri getBaseUri() {
        throw new ModelViewException();
    }

    @Override
    public ContentValues toValues() {
        throw new ModelViewException();
    }

    private class ModelViewException extends UnsupportedOperationException {
        ModelViewException() {
            super("This model is used as a view only!");
        }
    }
}
