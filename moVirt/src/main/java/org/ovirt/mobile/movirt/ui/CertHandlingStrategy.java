package org.ovirt.mobile.movirt.ui;

public enum CertHandlingStrategy {
    TRUST_SYSTEM(0),
    TRUST_CUSTOM(1),
    TRUST_ALL(2);

    long index;

    private CertHandlingStrategy(long index) {
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
}