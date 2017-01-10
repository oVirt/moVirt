package org.ovirt.mobile.movirt.model.base;

import android.content.ContentValues;

public abstract class OvirtNamedViewEntity extends OVirtNamedEntity {

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
