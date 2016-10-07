package org.ovirt.mobile.movirt.rest.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.ParseUtils;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Console implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Console> {
    public String id;
    public String address;
    public String protocol;
    public String port;
    public String tls_port;
    public Vm vm;

    public org.ovirt.mobile.movirt.model.Console toEntity() {
        org.ovirt.mobile.movirt.model.Console console = new org.ovirt.mobile.movirt.model.Console();
        console.setId(id);
        console.setAddress(address);
        console.setDisplayType(org.ovirt.mobile.movirt.model.Display.mapDisplay(protocol));
        console.setPort(ParseUtils.intOrDefault(port));
        console.setTlsPort(ParseUtils.intOrDefault(tls_port));
        if (vm != null) {
            console.setVmId(vm.id);
        }

        return console;
    }
}
