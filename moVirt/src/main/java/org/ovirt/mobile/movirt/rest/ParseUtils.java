package org.ovirt.mobile.movirt.rest;

import org.ovirt.mobile.movirt.rest.dto.Version;

public class ParseUtils {

    public static String parseVersion(Version version) {
        String versionString = "";
        if (version != null && version.major != null) {
            versionString = version.major;
        }

        if (version != null && version.minor != null) {
            versionString += "." + version.minor;
        }

        return versionString;
    }

    public static int intOrDefault(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return -1;
        }
    }
}
