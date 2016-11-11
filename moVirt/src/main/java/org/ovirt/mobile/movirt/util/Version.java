package org.ovirt.mobile.movirt.util;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Version implements Comparable<Version> {
    public static final int API_FALLBACK_MAJOR_VERSION = 3;
    public static final int API_FALLBACK_MINOR_VERSION = 0;
    public static final int API_FALLBACK_BUILD_VERSION = 0;

    private int major;
    private int minor;
    private int build;

    public Version() {
        this.major = API_FALLBACK_MAJOR_VERSION;
        this.minor = API_FALLBACK_MINOR_VERSION;
        this.build = API_FALLBACK_BUILD_VERSION;
    }

    public Version(int major, int minor, int build) {
        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    // serialization to authenticator's user data should not occur for these methods
    @JsonIgnore
    public boolean isV3Api() {
        return major < 4;
    }

    @JsonIgnore
    public boolean isV4Api() {
        return major >= 4;
    }

    public boolean isApiWithinRange(Version from, Version to) {
        return compareTo(from) >= 0 && compareTo(to) <= 0;
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", major, minor, build);
    }

    /**
     * @param another version to be compared to
     * @return a negative integer, zero, or a positive integer if this object version is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Version another) {
        if (major == another.major) {
            if (minor == another.minor) {
                return build - another.build;
            }
            return minor - another.minor;
        }
        return major - another.major;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;

        Version v = (Version) o;

        return major == v.major &&
                minor == v.minor &&
                build == v.build;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + build;

        return result;
    }
}
