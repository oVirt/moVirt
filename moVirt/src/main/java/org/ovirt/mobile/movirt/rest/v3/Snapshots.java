package org.ovirt.mobile.movirt.rest.v3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

/**
 * Created by suomiy on 11/25/15.
 */
public class Snapshots extends RestEntityWrapperList<Snapshot> {
    @JsonCreator
    public Snapshots(@JsonProperty("snapshot") List<Snapshot> list) {
        super(list);
    }
}
