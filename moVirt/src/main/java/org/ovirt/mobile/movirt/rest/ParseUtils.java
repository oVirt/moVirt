package org.ovirt.mobile.movirt.rest;

import org.ovirt.mobile.movirt.rest.dto.Version;

import java.net.MalformedURLException;
import java.net.URL;

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

    public static URL tryToParseUrl(String endpoint) {
        try {
            return new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL is not valid");
        }
    }
}
