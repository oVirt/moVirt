package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cluster {
    // public for json mapping
    public String id;
    public String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
