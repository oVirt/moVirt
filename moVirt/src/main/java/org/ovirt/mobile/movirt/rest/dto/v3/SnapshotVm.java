package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotVm extends org.ovirt.mobile.movirt.rest.dto.SnapshotVm {
    public Status status;
    public Cluster cluster;

    public org.ovirt.mobile.movirt.model.SnapshotVm toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.SnapshotVm vm = super.toEntity(accountId);
        vm.setStatus(Status.asVmStatus(status));
        vm.setClusterId(IdHelper.combinedIdSafe(accountId, cluster));

        return vm;
    }
}
