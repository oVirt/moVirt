package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Disk implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Disk> {
    public String id;
    public String name;
    public String size;
    public Status status;
    public Vm vm;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Status {
        public String state;
    }

    public org.ovirt.mobile.movirt.model.Disk toEntity() {
        org.ovirt.mobile.movirt.model.Disk disk = new org.ovirt.mobile.movirt.model.Disk();
        disk.setId(id);
        disk.setName(name);
        disk.setSize(size);
        disk.setStatus(status.state);
        disk.setVmId(vm.id);

        return disk;
    }
}
