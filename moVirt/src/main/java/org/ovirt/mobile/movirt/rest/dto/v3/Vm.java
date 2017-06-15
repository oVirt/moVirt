package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.RestMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm extends org.ovirt.mobile.movirt.rest.dto.Vm {
    public Status status;
    public Host host;
    public Cluster cluster;
    public Nics nics;

    public org.ovirt.mobile.movirt.model.Vm toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Vm vm = super.toEntity(accountId);
        vm.setStatus(Status.asVmStatus(status));
        vm.setClusterId(IdHelper.combinedIdSafe(accountId, cluster));
        vm.setHostId(IdHelper.combinedIdSafe(accountId, host));

        vm.setNics(RestMapper.mapToEntities(nics, accountId));

        return vm;
    }
}
