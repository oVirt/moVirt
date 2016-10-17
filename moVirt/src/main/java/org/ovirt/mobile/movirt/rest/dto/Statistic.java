package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class Statistic {
    public String name;
    public Values values;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Values {
        public List<Value> value;
    }

    static class Value {
        public String datum;
    }
}
