package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class HostEventFacade extends BaseEntityFacade<Event> {

    public HostEventFacade() {
        super(Event.class);
    }

    @Override
    protected Request<List<Event>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "hostId", "hostName");
        String hostName = ids[1];

        return oVirtClient.getHostEventsRequest(hostName);
    }

    @Override
    protected Response<List<Event>> getSyncAllResponse(Response<List<Event>> response, String... ids) {
        requireSignature(ids, "hostId", "hostName");
        String hostId = ids[0];

        return respond()
                .doNotRemoveExpired()
                .withBeforeInsertCallback(remoteEntity -> {
                    remoteEntity.setTemporary(true);
                    remoteEntity.setHostId(hostId);
                })
                .withBeforeUpdatabilityResolvedCallback((localEntity, remoteEntity) -> {
                    remoteEntity.setStorageDomainId(localEntity.getStorageDomainId());
                    remoteEntity.setHostId(hostId);
                    remoteEntity.setVmId(localEntity.getVmId());
                })
                .asUpdateEntitiesResponse()
                .addResponse(response);
    }
}
