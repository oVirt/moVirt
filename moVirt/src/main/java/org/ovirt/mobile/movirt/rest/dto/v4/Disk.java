package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Disk extends org.ovirt.mobile.movirt.rest.dto.Disk {
    public String status;
    public String provisioned_size;
    public Vm vm; // used up to 4.1

    public org.ovirt.mobile.movirt.model.Disk toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Disk disk = super.toEntity(accountId);
        disk.setStatus(status);
        disk.setSize(ObjectUtils.parseLong(provisioned_size));

        disk.setVmId(IdHelper.combinedIdSafe(accountId, vm));

        return disk;
    }
}
