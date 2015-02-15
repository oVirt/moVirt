package org.ovirt.mobile.movirt.sync.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class Statistic {
    public String name;
    public Values values;

    static class Values {
        public List<Value> value;
        public String type;
    }

    static class Value {
        public String datum;
    }
}
