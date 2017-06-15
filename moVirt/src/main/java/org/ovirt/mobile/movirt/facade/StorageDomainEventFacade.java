package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class StorageDomainEventFacade extends BaseEntityFacade<Event> {

    public StorageDomainEventFacade() {
        super(Event.class);
    }

    @Override
    protected Request<List<Event>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "storageDomainId", "storageDomainName");
        String sdName = ids[1];

        return oVirtClient.getStorageDomainEventsRequest(sdName);
    }

    @Override
    protected Response<List<Event>> getSyncAllResponse(Response<List<Event>> response, String... ids) {
        requireSignature(ids, "storageDomainId", "storageDomainName");
        String sdId = ids[0];

        return respond()
                .doNotRemoveExpired()
                .withBeforeInsertCallback(remoteEntity -> {
                    remoteEntity.setTemporary(true);
                    remoteEntity.setStorageDomainId(sdId);
                })
                .withBeforeUpdatabilityResolvedCallback((localEntity, remoteEntity) -> {
                    remoteEntity.setStorageDomainId(sdId);
                    remoteEntity.setHostId(localEntity.getHostId());
                    remoteEntity.setVmId(localEntity.getVmId());
                })
                .asUpdateEntitiesResponse()
                .addResponse(response);
    }
}
