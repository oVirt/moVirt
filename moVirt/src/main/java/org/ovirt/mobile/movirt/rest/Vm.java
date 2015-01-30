package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Vm> {
    // public for json mapping

    public String id;
    public String name;
    public Status status;
    public Cluster cluster;

    // status complex object in rest
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        public String state;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cluster {
        public String id;
    }

    @Override
    public String toString() {
        return String.format("Vm: name=%s, id=%s, status=%s, clusterId=%s",
                             name, id, status.state, cluster.id);
    }

    public org.ovirt.mobile.movirt.model.Vm toEntity() {
        org.ovirt.mobile.movirt.model.Vm vm = new org.ovirt.mobile.movirt.model.Vm();
        vm.setId(id);
        vm.setName(name);
        vm.setStatus(mapStatus(status.state));
        vm.setClusterId(cluster.id);

        return vm;
    }

    private static org.ovirt.mobile.movirt.model.Vm.Status mapStatus(String status) {
        return org.ovirt.mobile.movirt.model.Vm.Status.valueOf(status.toUpperCase());
    }

}
