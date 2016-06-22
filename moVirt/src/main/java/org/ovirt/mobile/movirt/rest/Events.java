package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

class Events extends RestEntityWrapperList<Event> {
    @JsonCreator
    public Events(@JsonProperty("event") List<Event> list) {
        super(list);
    }
}
