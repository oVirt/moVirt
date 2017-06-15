package org.ovirt.mobile.movirt.model.base;

import android.content.ContentValues;

public abstract class OvirtAccountNamedViewEntity extends OVirtAccountNamedEntity {

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
