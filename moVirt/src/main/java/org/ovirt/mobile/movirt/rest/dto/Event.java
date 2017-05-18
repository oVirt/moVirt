package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.EventSeverity;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.util.IdHelper;

import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Event>, HasId {

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IdRef implements HasId {
        public String id;

        @Override
        public String getId() {
            return id;
        }
    }

    @Override
    public String getId() {
        return Integer.toString(id);
    }

    @Override
    public org.ovirt.mobile.movirt.model.Event toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Event event = new org.ovirt.mobile.movirt.model.Event();
        event.setIds(accountId, getId());
        event.setCode(code);
        event.setDescription(description);
        event.setSeverity(EventSeverity.fromString(severity));
        event.setTime(new Timestamp(time));

        event.setVmId(IdHelper.combinedIdSafe(accountId, vm));
        event.setHostId(IdHelper.combinedIdSafe(accountId, host));
        event.setClusterId(IdHelper.combinedIdSafe(accountId, cluster));
        event.setStorageDomainId(IdHelper.combinedIdSafe(accountId, storage_domain));
        event.setDataCenterId(IdHelper.combinedIdSafe(accountId, data_center));

        return event;
    }
}
