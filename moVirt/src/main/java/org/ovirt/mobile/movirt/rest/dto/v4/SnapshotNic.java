package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotNic extends org.ovirt.mobile.movirt.rest.dto.SnapshotNic {
    public Vm vm;

    public org.ovirt.mobile.movirt.model.SnapshotNic toEntity() {
        org.ovirt.mobile.movirt.model.SnapshotNic nic = super.toEntity();
        if (vm != null) {
            nic.setVmId(vm.id);
        }

        return nic;
    }
}
