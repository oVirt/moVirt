package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

/**
 * Created by sphoorti on 5/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Disk implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Disk> {
    public String id;
    public String name;
    public String actual_size;

    public org.ovirt.mobile.movirt.model.Disk toEntity() {
        org.ovirt.mobile.movirt.model.Disk disk = new org.ovirt.mobile.movirt.model.Disk();
        disk.setId(id);
        disk.setName(name);
        disk.setUsedSize(ObjectUtils.parseLong(actual_size));

        return disk;
    }
}
