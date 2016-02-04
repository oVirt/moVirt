package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.storage.StorageDomainDetailActivity_;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.*;

@EBean
public class StorageDomainFacade extends BaseEntityFacade<StorageDomain> {

    public StorageDomainFacade() {
        super(StorageDomain.class);
    }

    @Override
    public Intent getDetailIntent(StorageDomain entity, Context context) {
        Intent intent = new Intent(context, StorageDomainDetailActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(entity.getUri());
        return intent;
    }

    @Override
    protected OVirtClient.Request<StorageDomain> getSyncOneRestRequest(String storageId, String... ids) {
        requireSignature(ids);
        return oVirtClient.getStorageDomainRequest(storageId);
    }

    @Override
    protected OVirtClient.Request<List<StorageDomain>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids);
        return oVirtClient.getStorageDomainsRequest();
    }
}
