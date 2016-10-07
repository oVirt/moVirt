package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Version {
    public String major;
    public String minor;
    public String build;
    public String full_version;

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }

    public String getBuild() {
        return build;
    }

    public String getFullVersion() {
        return full_version;
    }
}
