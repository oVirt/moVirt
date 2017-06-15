package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cluster extends org.ovirt.mobile.movirt.rest.dto.Cluster {
    public DataCenter data_center;

    @Override
    public org.ovirt.mobile.movirt.model.Cluster toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Cluster cluster = super.toEntity(accountId);

        cluster.setDataCenterId(IdHelper.combinedIdSafe(accountId, data_center));

        return cluster;
    }
}
