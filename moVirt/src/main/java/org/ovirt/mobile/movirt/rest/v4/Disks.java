package org.ovirt.mobile.movirt.rest.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class Disks extends RestEntityWrapperList<Disk> {
    @JsonCreator
    public Disks(@JsonProperty("disk") List<Disk> list) {
        super(list);
    }
}
