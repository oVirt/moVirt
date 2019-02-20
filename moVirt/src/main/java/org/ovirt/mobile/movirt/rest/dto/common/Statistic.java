package org.ovirt.mobile.movirt.rest.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Statistic {
    public String name;
    public Values values;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Values {
        public List<Value> value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        public String datum;
    }
}
