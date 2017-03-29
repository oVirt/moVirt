package org.ovirt.mobile.movirt.auth.properties.property.version.support;

import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.auth.properties.property.version.exceptions.SupportedUntilException;
import org.ovirt.mobile.movirt.auth.properties.property.version.exceptions.UnsupportedUntilException;

public enum VersionSupport {
    DISK_ATTACHMENTS(new Version(4, 0, 1), Version.MAX_VALUE),
    VM_DISKS(Version.MIN_VALUE, new Version(4, 0, 1));

    private Version supportedFrom;
    private Version supportedUntil;

    VersionSupport(Version supportedFrom, Version supportedUntil) {
        this.supportedFrom = supportedFrom;
        this.supportedUntil = supportedUntil;
    }

    public Version getVersion() {
        return supportedFrom;
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

    private boolean isSupportedFrom(Version currentVersion) {
        return supportedFrom.compareTo(currentVersion) <= 0;
    }

    private boolean isSupportedUntil(Version currentVersion) {
        return supportedUntil.compareTo(currentVersion) > 0;
    }
}
