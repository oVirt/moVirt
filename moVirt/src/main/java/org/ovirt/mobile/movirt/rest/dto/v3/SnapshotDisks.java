package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class SnapshotDisks extends RestEntityWrapperList<SnapshotDisk> {
    @JsonCreator
    public SnapshotDisks(@JsonProperty("disk") List<SnapshotDisk> list) {
        super(list);
    }
}
