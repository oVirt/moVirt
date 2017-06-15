package org.ovirt.mobile.movirt.auth.properties.property;

import android.support.annotation.NonNull;

import org.springframework.util.StringUtils;

public enum CertLocation {
    DEFAULT_URL(0),
    CUSTOM_URL(1),
    FILE(2);

    private final int index;

    CertLocation(int index) {
        this.index = index;
    }

    public int id() {
        return index;
    }

    public boolean is(long selected) {
        return index == selected;
    }

    @NonNull
    public static CertLocation from(long index) {
        if (CUSTOM_URL.is(index)) {
            return CUSTOM_URL;
        } else if (FILE.is(index)) {
            return FILE;
        }

        return DEFAULT_URL;
    }

    @NonNull
    public static CertLocation fromString(String certLocation) {
        if (!StringUtils.isEmpty(certLocation)) {
            try {
                return valueOf(certLocation);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return CertLocation.DEFAULT_URL;
    }

}
