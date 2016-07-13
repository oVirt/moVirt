package org.ovirt.mobile.movirt.rest.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class Clusters extends RestEntityWrapperList<Cluster> {
    @JsonCreator
    public Clusters(@JsonProperty("cluster") List<Cluster> list) {
        super(list);
    }
}
