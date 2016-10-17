package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class Vms extends RestEntityWrapperList<Vm> {
    @JsonCreator
    public Vms(@JsonProperty("vm") List<Vm> list) {
        super(list);
    }
}
