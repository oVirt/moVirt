package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Disk {
    public String name;
    public String size;
    public Status status;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Status {
        public String state;
    }
}
