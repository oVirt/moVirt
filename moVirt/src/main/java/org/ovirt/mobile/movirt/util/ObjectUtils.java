package org.ovirt.mobile.movirt.util;

/**
 * Backport of utils available only from API level 19
 */
public final class ObjectUtils {
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
