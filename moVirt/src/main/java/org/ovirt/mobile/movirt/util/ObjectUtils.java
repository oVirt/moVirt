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

    /**
     * @param o    object to be checked against
     * @param name name of the object o
     * @throws IllegalArgumentException if o is null
     */
    public static void requireNotNull(Object o, String name) {
        if (o == null) {
            throw new IllegalArgumentException(name + " cannot be null.");
        }
    }

    /**
     * @param value value to be converted
     * @return int, unknown types are set to -1
     */
    public static int convertToInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return -1;
        }
    }

    /**
     * @param value value to be parsed
     * @return long of value, -1 if it cannot be parsed
     */
    public static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return -1;
        }
    }
}
