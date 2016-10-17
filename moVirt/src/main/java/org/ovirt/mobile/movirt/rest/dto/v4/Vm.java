package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.RestHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm extends org.ovirt.mobile.movirt.rest.dto.Vm {
    public String status;
    public Host host;
    public Cluster cluster;
    public Disks disks;
    public Nics nics;

    @Override
    public String toString() {
        return String.format("%s, status=%s, clusterId=%s", super.toString(), status, cluster.id);
    }

    public org.ovirt.mobile.movirt.model.Vm toEntity() {
        org.ovirt.mobile.movirt.model.Vm vm = super.toEntity();
        vm.setStatus(mapStatus(status));
        if (cluster != null) {
            vm.setClusterId(cluster.id);
        }
        vm.setHostId(host != null ? host.id : "");

        vm.setNics(RestHelper.mapToEntities(nics));
        vm.setDisks(RestHelper.mapToEntities(disks));
        return vm;
    }

    private static org.ovirt.mobile.movirt.model.Vm.Status mapStatus(String status) {
        try {
            return org.ovirt.mobile.movirt.model.Vm.Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.Vm.Status.UNKNOWN;
        }
    }
}
