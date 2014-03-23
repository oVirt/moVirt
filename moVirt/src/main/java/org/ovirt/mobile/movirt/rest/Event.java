package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
class Event implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Event> {
    public int id;
    public int code;
    public String description;
    public String severity;
    public long time;

    public IdRef vm;
    public IdRef host;
    public IdRef cluster;
    public IdRef data_center;
    public IdRef storage_domain;

    static class IdRef {
        public String id;
    }

    @Override
    public org.ovirt.mobile.movirt.model.Event toEntity() {
        org.ovirt.mobile.movirt.model.Event event = new org.ovirt.mobile.movirt.model.Event();
        event.setId(id);
        event.setCode(code);
        event.setDescription(description);
        event.setSeverity(org.ovirt.mobile.movirt.model.Event.Severity.valueOf(severity.toUpperCase()));
        event.setTime(new Timestamp(time));
        event.setVmId(vm.id);
        event.setHostId(host.id);
        event.setClusterId(cluster.id);
        event.setStorageDomainId(storage_domain.id);
        event.setDataCenterId(data_center.id);
        return event;
    }
}
