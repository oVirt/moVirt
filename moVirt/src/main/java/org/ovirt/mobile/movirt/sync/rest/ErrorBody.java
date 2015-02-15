package org.ovirt.mobile.movirt.sync.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorBody {
    public Fault fault;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fault {

        public String reason;

        public String detail;
    }
}
