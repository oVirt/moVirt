package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class Events extends RestEntityWrapperList<Event> {
    @JsonCreator
    public Events(@JsonProperty("event") List<Event> list) {
        super(list);
    }
}
