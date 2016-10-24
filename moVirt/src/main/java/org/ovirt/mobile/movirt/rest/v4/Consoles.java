package org.ovirt.mobile.movirt.rest.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class Consoles extends RestEntityWrapperList<Console> {
    @JsonCreator
    public Consoles(@JsonProperty("graphics_console") List<Console> list) {
        super(list);
    }
}
