package org.ovirt.mobile.movirt.facade;

import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.SyncAdapter;

@EBean
public abstract class BaseEntityFacade<E extends OVirtEntity> implements EntityFacade<E> {

    @Bean
    SyncAdapter syncAdapter;

    private final Class<E> clazz;

    protected BaseEntityFacade(Class<E> clazz) {
        this.clazz = clazz;
    }

    protected abstract OVirtClient.Request<E> getRestRequest(String id);

    @Override
    public E mapFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(clazz).fromCursor(cursor);
    }

    @Override
    public void sync(String id, OVirtClient.Response<E> response) {
        syncAdapter.syncEntity(clazz, getRestRequest(id), response);
    }
}
