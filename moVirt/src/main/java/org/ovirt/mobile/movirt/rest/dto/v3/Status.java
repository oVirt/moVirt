package org.ovirt.mobile.movirt.rest.dto.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.DataCenterStatus;
import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.model.enums.StorageDomainStatus;
import org.ovirt.mobile.movirt.model.enums.VmStatus;

// status complex object in rest
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
    public String state;

    public static HostStatus asHostStatus(Status status) {
        return HostStatus.fromString(status == null ? null : status.state);
    }

    public static StorageDomainStatus asStorageDomainStatus(Status status) {
        return StorageDomainStatus.fromString(status == null ? null : status.state);
    }

    public static VmStatus asVmStatus(Status status) {
        return VmStatus.fromString(status == null ? null : status.state);
    }

    public static DataCenterStatus asDataCenterStatus(Status status) {
        return DataCenterStatus.fromString(status == null ? null : status.state);
    }
}
