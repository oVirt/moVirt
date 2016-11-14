package org.ovirt.mobile.movirt.auth.properties;

import android.text.TextUtils;

import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.util.JsonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParseUtils {

    public static String[] parseHostnames(String hostnames) {
        if (TextUtils.isEmpty(hostnames)) {
            return new String[]{};
        }

        List<String> validForHostnames = new ArrayList<>();
        for (String hostname : hostnames.split("[,\\s]")) {
            if (!hostname.isEmpty()) {
                validForHostnames.add(hostname);
            }
        }

        return Arrays.copyOf(validForHostnames.toArray(), validForHostnames.size(), String[].class);
    }

    public static String catenateToCsv(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (String n : array) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(n);
        }
        return sb.toString();
    }

    /**
     * @param value value to be converted
     * @return String, unknown types are converted to JSON
     */
    public static String convertToString(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Boolean) {
            return Boolean.toString((Boolean) value);
        } else if (value instanceof Long) {
            return Long.toString((Long) value);
        } else if (value instanceof CertHandlingStrategy) {
            return Long.toString(((CertHandlingStrategy) value).id());
        } else {
            return JsonUtils.objectToString(value);
        }
    }
}
