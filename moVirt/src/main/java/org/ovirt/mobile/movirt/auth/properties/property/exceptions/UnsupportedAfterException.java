package org.ovirt.mobile.movirt.auth.properties.property.exceptions;

import org.ovirt.mobile.movirt.auth.properties.property.Version;

public class UnsupportedAfterException extends UnsupportedOperationException {

    public UnsupportedAfterException(Version unsupportedAfter) {
        this(unsupportedAfter, "This operation");
    }

    public UnsupportedAfterException(Version unsupportedAfter, String what) {
        super(String.format("%s is not supported after API version %s", what, unsupportedAfter));
    }
}
