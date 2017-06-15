package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotVm extends org.ovirt.mobile.movirt.rest.dto.SnapshotVm {
    public String status;
    public Cluster cluster;

    public org.ovirt.mobile.movirt.model.SnapshotVm toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.SnapshotVm vm = super.toEntity(accountId);
        vm.setStatus(VmStatus.fromString(status));
        vm.setClusterId(IdHelper.combinedIdSafe(accountId, cluster));

        return vm;
    }
}
