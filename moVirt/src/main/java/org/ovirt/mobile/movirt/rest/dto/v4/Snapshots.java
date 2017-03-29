package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class Snapshots extends RestEntityWrapperList<Snapshot> {
    @JsonCreator
    public Snapshots(@JsonProperty("snapshot") List<Snapshot> list) {
        super(list);
    }
}
