package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.ConsoleProtocol;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Console implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Console> {
    public String id;
    public String protocol;
    public org.ovirt.mobile.movirt.rest.Vm vm;

    public org.ovirt.mobile.movirt.model.Console toEntity() {
        org.ovirt.mobile.movirt.model.Console console = new org.ovirt.mobile.movirt.model.Console();
        console.setId(id);
        console.setProtocol(ConsoleProtocol.mapProtocol(protocol));
        if (vm != null) {
            console.setVmId(vm.id);
        }

        return console;
    }
}
