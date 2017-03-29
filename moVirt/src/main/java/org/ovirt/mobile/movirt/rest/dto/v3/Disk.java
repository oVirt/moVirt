package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.ObjectUtils;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Disk extends org.ovirt.mobile.movirt.rest.dto.Disk {
    public Status status;
    public String size;
    public Vm vm;

    public org.ovirt.mobile.movirt.model.Disk toEntity() {
        org.ovirt.mobile.movirt.model.Disk disk = super.toEntity();
        disk.setSize(ObjectUtils.parseLong(size));

        if (status != null) {
            disk.setStatus(status.state);
        }

        if (vm != null) {
            disk.setVmId(vm.id);
        }

        return disk;
    }
}
