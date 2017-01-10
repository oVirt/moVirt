package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DiskAttachment implements RestEntityWrapper<org.ovirt.mobile.movirt.model.DiskAttachment> {

    // public String id; // currently same as diskId

    public org.ovirt.mobile.movirt.model.DiskAttachment toEntity() {
        return new org.ovirt.mobile.movirt.model.DiskAttachment();
    }
}
