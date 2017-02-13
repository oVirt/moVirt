package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotVm extends org.ovirt.mobile.movirt.rest.dto.SnapshotVm {
    public Status status;
    public Cluster cluster;

    public org.ovirt.mobile.movirt.model.SnapshotVm toEntity() {
        org.ovirt.mobile.movirt.model.SnapshotVm vm = super.toEntity();
        vm.setStatus(Status.asVmStatus(status));
        if (cluster != null) {
            vm.setClusterId(cluster.id);
        }

        return vm;
    }
}
