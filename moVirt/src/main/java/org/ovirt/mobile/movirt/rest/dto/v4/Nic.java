package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by yixin on 2015/3/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Nic extends org.ovirt.mobile.movirt.rest.dto.Nic {
    public Vm vm;

    public org.ovirt.mobile.movirt.model.Nic toEntity() {
        org.ovirt.mobile.movirt.model.Nic nic = super.toEntity();
        if (vm != null) {
            nic.setVmId(vm.id);
        }

        return nic;
    }

}
