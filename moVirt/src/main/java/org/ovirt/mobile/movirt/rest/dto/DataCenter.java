package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.ParseUtils;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DataCenter implements RestEntityWrapper<org.ovirt.mobile.movirt.model.DataCenter>, HasId {
    public String id;
    public String name;
    public Version version;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public org.ovirt.mobile.movirt.model.DataCenter toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.DataCenter dataCenter = new org.ovirt.mobile.movirt.model.DataCenter();
        dataCenter.setIds(accountId, id);
        dataCenter.setName(name);
        dataCenter.setVersion(ParseUtils.parseVersion(version));

        return dataCenter;
    }
}
