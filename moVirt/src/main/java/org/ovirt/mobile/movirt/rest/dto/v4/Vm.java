package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.util.RestMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm extends org.ovirt.mobile.movirt.rest.dto.Vm {
    public String status;
    public Host host;
    public Cluster cluster;
    public Nics nics;

    public org.ovirt.mobile.movirt.model.Vm toEntity() {
        org.ovirt.mobile.movirt.model.Vm vm = super.toEntity();
        vm.setStatus(VmStatus.fromString(status));
        if (cluster != null) {
            vm.setClusterId(cluster.id);
        }
        vm.setHostId(host != null ? host.id : "");

        vm.setNics(RestMapper.mapToEntities(nics));
        return vm;
    }
}
