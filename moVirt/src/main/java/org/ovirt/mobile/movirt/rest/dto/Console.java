package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.ConsoleProtocol;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.util.IdHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Console implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Console>, HasId {

    public String id;
    public String protocol;
    public org.ovirt.mobile.movirt.rest.dto.Vm vm;

    public org.ovirt.mobile.movirt.model.Console toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Console console = new org.ovirt.mobile.movirt.model.Console();
        console.setIds(accountId, id);
        console.setProtocol(ConsoleProtocol.mapProtocol(protocol));
        console.setVmId(IdHelper.combinedIdSafe(accountId, vm));

        return console;
    }

    @Override
    public String getId() {
        return id;
    }
}
