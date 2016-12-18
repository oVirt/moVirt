package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class DiskAttachments extends RestEntityWrapperList<DiskAttachment> {
    @JsonCreator
    public DiskAttachments(@JsonProperty("disk_attachment") List<DiskAttachment> list) {
        super(list);
    }
}
