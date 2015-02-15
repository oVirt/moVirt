package org.ovirt.mobile.movirt.sync.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// status complex object in rest
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
    public String state;
}
