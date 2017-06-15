package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Disk extends org.ovirt.mobile.movirt.rest.dto.Disk {
    public Status status;
    public String size;
    public Vm vm;

    public org.ovirt.mobile.movirt.model.Disk toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Disk disk = super.toEntity(accountId);
        disk.setSize(ObjectUtils.parseLong(size));

        if (status != null) {
            disk.setStatus(status.state);
        }

        disk.setVmId(IdHelper.combinedIdSafe(accountId, vm));

        return disk;
    }
}
