package org.ovirt.mobile.movirt.auth.properties.property.version.exceptions;

import org.ovirt.mobile.movirt.auth.properties.property.version.Version;

public class UnsupportedUntilException extends UnsupportedOperationException {

    public UnsupportedUntilException(Version supportedFrom, Version currentVersion, String what) {
        super(String.format("%s is not supported until API version %s. Current version is %s.", what, supportedFrom, currentVersion));
    }
}
