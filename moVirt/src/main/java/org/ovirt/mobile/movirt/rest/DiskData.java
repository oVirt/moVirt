package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonRootName("disks")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiskData {
    public String name;
    public String size;
    public Status status;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Status {
        String status;
    }
}
