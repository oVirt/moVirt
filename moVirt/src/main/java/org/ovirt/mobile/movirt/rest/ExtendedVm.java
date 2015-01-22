package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.VmStatistics;

/**
 * Additional parameters which are not going to be persisted
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedVm extends Vm {

    public Display display;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Display {
        public String address, port, type;
    }

}
