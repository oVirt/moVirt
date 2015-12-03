package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.ui.storage.StorageDomainDetailActivity_;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EBean
public class StorageDomainFacade extends BaseEntityFacade<StorageDomain> {

    @Bean
    OVirtClient oVirtClient;

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
    public Collection<Trigger<StorageDomain>> getAllTriggers() {
        //TODO: StorageDomainTriggerResolver not implemented, so return an empty list
        return new ArrayList<>();
    }

    @Override
    public List<Trigger<StorageDomain>> getTriggers(StorageDomain entity, Collection<Trigger<StorageDomain>> allTriggers) {
        //TODO: StorageDomainTriggerResolver not implemented, so return an empty list
        return new ArrayList<>();
    }

    @Override
    protected OVirtClient.Request<StorageDomain> getRestRequest(String id) {
        return oVirtClient.getStorageDomainRequest(id);
    }
}
