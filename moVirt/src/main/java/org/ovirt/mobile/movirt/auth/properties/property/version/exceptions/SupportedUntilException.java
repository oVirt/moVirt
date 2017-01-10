package org.ovirt.mobile.movirt.auth.properties.property.version.exceptions;

import org.ovirt.mobile.movirt.auth.properties.property.version.Version;

public class SupportedUntilException extends UnsupportedOperationException {

    public SupportedUntilException(Version supportedUntil, Version currentVersion, String what) {
        super(String.format("%s is supported until API version %s. Current version is %s.", what, supportedUntil, currentVersion));
    }
}
