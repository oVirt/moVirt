package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class VmEventFacade extends BaseEntityFacade<Event> {

    public VmEventFacade() {
        super(Event.class);
    }

    @Override
    protected Request<List<Event>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId", "vmName");
        String vmName = ids[1];

        return oVirtClient.getVmEventsRequest(vmName);
    }

    @Override
    protected Response<List<Event>> getSyncAllResponse(Response<List<Event>> response, String... ids) {
        requireSignature(ids, "vmId", "vmName");
        String vmId = ids[0];

        return respond()
                .doNotRemoveExpired()
                .withBeforeInsertCallback(remoteEntity -> {
                    remoteEntity.setTemporary(true);
                    remoteEntity.setVmId(vmId);
                })
                .withBeforeUpdatabilityResolvedCallback((localEntity, remoteEntity) -> {
                    remoteEntity.setStorageDomainId(localEntity.getStorageDomainId());
                    remoteEntity.setHostId(localEntity.getHostId());
                    remoteEntity.setVmId(vmId);
                })
                .asUpdateEntitiesResponse()
                .addResponse(response);
    }
}
