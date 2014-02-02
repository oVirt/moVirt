package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm {
    // public for json mapping
    public String id;
    public String name;

    // status complex object in rest
    public static class Status {
        public String state;
    }

    // public for json mapping
    public Status status;

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status.state;
    }
}
