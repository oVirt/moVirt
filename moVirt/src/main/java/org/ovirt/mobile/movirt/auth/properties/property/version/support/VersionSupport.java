package org.ovirt.mobile.movirt.auth.properties.property.version.support;

import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.auth.properties.property.version.exceptions.SupportedUntilException;
import org.ovirt.mobile.movirt.auth.properties.property.version.exceptions.UnsupportedUntilException;

public enum VersionSupport {
    DISK_ATTACHMENTS(new Version(4, 0, 1), Version.MAX_VALUE),
    VM_DISKS(Version.MIN_VALUE, new Version(4, 0, 1)),

    NICS_POLLED_WITH_VMS(Version.MIN_VALUE, Version.V4),

    PLUS_SIGN_IN_PASSWORD(new Version(4, 1, 2), Version.MAX_VALUE),

    OVIRT_ENGINE(new Version(3, 6, 0), Version.MAX_VALUE),

    FOLLOW_LINKS(new Version(4, 2, 0), Version.MAX_VALUE),

    FOLLOW_BUG(new Version(4, 2, 0), new Version(4, 2, 2));

    private Version supportedFrom;
    private Version supportedUntil;

    VersionSupport(Version supportedFrom, Version supportedUntil) {
        this.supportedFrom = supportedFrom;
        this.supportedUntil = supportedUntil;
    }

    /**
     * Inclusive
     */
    public Version getSupportedFrom() {
        return supportedFrom;
    }

    /**
     * Exclusive
     */
    public Version getSupportedUntil() {
        return supportedUntil;
    }

    public boolean isSupported(Version currentVersion) {
        return isSupportedFrom(currentVersion) && isSupportedUntil(currentVersion);
    }

    public void throwIfNotSupported(Version currentVersion) throws UnsupportedOperationException {
        if (!isSupportedFrom(currentVersion)) {
            throw new UnsupportedUntilException(supportedFrom, currentVersion, name());
        }

        if (!isSupportedUntil(currentVersion)) {
            throw new SupportedUntilException(supportedUntil, currentVersion, name());
        }
    }

    /**
     * Inclusive
     */
    private boolean isSupportedFrom(Version currentVersion) {
        return supportedFrom.compareTo(currentVersion) <= 0;
    }

    /**
     * Exclusive
     */
    private boolean isSupportedUntil(Version currentVersion) {
        return supportedUntil.compareTo(currentVersion) > 0;
    }
}
