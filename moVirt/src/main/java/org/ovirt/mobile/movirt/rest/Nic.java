package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by yixin on 2015/3/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Nic implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Nic> {
    public String id;
    public String name;
    public boolean linked;
    public Mac mac;
    public boolean active;
    public boolean plugged;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Mac {
        public String address;
    }


    public org.ovirt.mobile.movirt.model.Nic toEntity() {
        org.ovirt.mobile.movirt.model.Nic nic = new org.ovirt.mobile.movirt.model.Nic();
        nic.setId(id);
        nic.setName(name);
        nic.setLinked(linked);
        nic.setMacAddress(mac.address);
        nic.setActive(active);
        nic.setPlugged(plugged);

        return nic;
    }

}
