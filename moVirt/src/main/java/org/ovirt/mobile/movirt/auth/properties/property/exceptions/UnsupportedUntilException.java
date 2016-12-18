package org.ovirt.mobile.movirt.auth.properties.property.exceptions;

import org.ovirt.mobile.movirt.auth.properties.property.Version;

public class UnsupportedUntilException extends UnsupportedOperationException {

    public UnsupportedUntilException(Version unsupportedUntil) {
        this(unsupportedUntil, "This operation");
    }

    public UnsupportedUntilException(Version unsupportedUntil, String what) {
        super(String.format("%s is not supported until API version %s", what, unsupportedUntil));
    }
}
