package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.rest.Request;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class StorageDomainFacade extends BaseEntityFacade<StorageDomain> {

    public StorageDomainFacade() {
        super(StorageDomain.class);
    }

    @Override
    protected Request<StorageDomain> getSyncOneRestRequest(String storageId, String... ids) {
        requireSignature(ids);
        return oVirtClient.getStorageDomainRequest(storageId);
    }

    @Override
    protected Request<List<StorageDomain>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids);
        return oVirtClient.getStorageDomainsRequest();
    }
}
