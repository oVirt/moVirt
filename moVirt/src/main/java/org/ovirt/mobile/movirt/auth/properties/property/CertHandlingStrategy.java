package org.ovirt.mobile.movirt.auth.properties.property;

import android.support.annotation.NonNull;

import org.springframework.util.StringUtils;

public enum CertHandlingStrategy {
    TRUST_SYSTEM(0),
    TRUST_CUSTOM(1),
    TRUST_ALL(2);

    private final long index;

    CertHandlingStrategy(long index) {
        this.index = index;
    }

    public boolean isCustomCertOptionsVisible() {
        if (TRUST_CUSTOM.is(index)) {
            return true;
        }

        return false;
    }

    public long id() {
        return index;
    }

    public boolean is(long selected) {
        return index == selected;
    }

    @NonNull
    public static CertHandlingStrategy from(long index) {
        if (TRUST_SYSTEM.is(index)) {
            return TRUST_SYSTEM;
        }

        if (TRUST_CUSTOM.is(index)) {
            return TRUST_CUSTOM;
        }

        if (TRUST_ALL.is(index)) {
            return TRUST_ALL;
        }

        return TRUST_SYSTEM;
    }

    @NonNull
    public static CertHandlingStrategy fromString(String strategy) {
        if (!StringUtils.isEmpty(strategy)) {
            try {
                return valueOf(strategy);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return CertHandlingStrategy.TRUST_SYSTEM;
    }
}
