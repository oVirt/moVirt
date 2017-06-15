package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotNic extends org.ovirt.mobile.movirt.rest.dto.SnapshotNic {
    public Vm vm;

    public org.ovirt.mobile.movirt.model.SnapshotNic toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.SnapshotNic nic = super.toEntity(accountId);
        nic.setVmId(IdHelper.combinedIdSafe(accountId, vm));

        return nic;
    }
}
