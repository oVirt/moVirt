package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Host extends org.ovirt.mobile.movirt.rest.dto.Host {
    public String status;
    public Cluster cluster;

    @Override
    public org.ovirt.mobile.movirt.model.Host toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Host host = super.toEntity(accountId);
        host.setStatus(HostStatus.fromString(status));
        host.setClusterId(IdHelper.combinedIdSafe(accountId, cluster));

        return host;
    }
}
