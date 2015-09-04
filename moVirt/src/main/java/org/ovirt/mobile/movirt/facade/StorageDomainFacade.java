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
public class StorageDomainFacade implements EntityFacade<StorageDomain> {

    @Bean
    SyncAdapter syncAdapter;

    @Override
    public StorageDomain mapFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(StorageDomain.class).fromCursor(cursor);
    }

    @Override
    public Intent getDetailIntent(StorageDomain entity, Context context) {
        Intent intent = new Intent(context, StorageDomainDetailActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(entity.getUri());
        return intent;
    }

    @Override
    public void sync(String id, OVirtClient.Response<StorageDomain> response) {
        syncAdapter.syncStorageDomain(id, response);
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
}
