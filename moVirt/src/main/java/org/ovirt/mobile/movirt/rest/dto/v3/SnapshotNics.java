package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class SnapshotNics extends RestEntityWrapperList<SnapshotNic> {
    @JsonCreator
    public SnapshotNics(@JsonProperty("nic") List<SnapshotNic> list) {
        super(list);
    }
}
