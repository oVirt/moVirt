package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Additional parameters which are not going to be persisted
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedVm extends Vm {

    public String memory;
    public Display display;
    public Os os;
    public Cpu cpu;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Display {
        public String address, port, type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Os {
        public String type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cpu {
        public Topology topology;
    }
}
