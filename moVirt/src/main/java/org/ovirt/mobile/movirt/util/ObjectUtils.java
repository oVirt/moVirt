package org.ovirt.mobile.movirt.util;

public final class ObjectUtils {
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    /**
     * Checks if ids has the same length as number of params,
     *
     * @param ids
     * @param params
     * @throws IllegalArgumentException if wrong number of params
     */
    public static void requireSignature(String[] ids, String... params) {
        int length = params.length;

        if (ids.length != length) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Expected %d parameters", length));
            if (length > 0) {
                sb.append(":");

                for (String p : params) {
                    sb.append(" ").append(p);
                }
            }

            throw new IllegalArgumentException(sb.toString());
        }
    }
}
