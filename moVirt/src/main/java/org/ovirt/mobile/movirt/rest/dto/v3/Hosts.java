package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class Hosts extends RestEntityWrapperList<Host> {
    @JsonCreator
    public Hosts(@JsonProperty("host") List<Host> list) {
        super(list);
    }
}
