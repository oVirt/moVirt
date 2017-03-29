package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.VmStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotVm extends org.ovirt.mobile.movirt.rest.dto.SnapshotVm {
    public String status;
    public Cluster cluster;

    public org.ovirt.mobile.movirt.model.SnapshotVm toEntity() {
        org.ovirt.mobile.movirt.model.SnapshotVm vm = super.toEntity();
        vm.setStatus(VmStatus.fromString(status));
        if (cluster != null) {
            vm.setClusterId(cluster.id);
        }

        return vm;
    }
}
