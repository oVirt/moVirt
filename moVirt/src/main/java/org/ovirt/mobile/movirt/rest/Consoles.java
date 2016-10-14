package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Consoles extends RestEntityWrapperList<Console> {
    @JsonCreator
    public Consoles(@JsonProperty("graphics_console") List<Console> list) {
        super(list);
    }
}
