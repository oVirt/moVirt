package org.ovirt.mobile.movirt.facade;

import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.SyncAdapter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EBean
public abstract class BaseEntityFacade<E extends OVirtEntity> implements EntityFacade<E> {

    @Bean
    SyncAdapter syncAdapter;

    @Bean
    OVirtClient oVirtClient;

    private final Class<E> clazz;

    protected BaseEntityFacade(Class<E> clazz) {
        this.clazz = clazz;
    }

    protected abstract OVirtClient.Request<E> getSyncOneRestRequest(String id, String... ids);

    protected abstract OVirtClient.Request<List<E>> getSyncAllRestRequest(String... ids);

    protected OVirtClient.CompositeResponse<E> getSyncOneResponse(final OVirtClient.Response<E> response, String... ids) {
        return new OVirtClient.CompositeResponse<>(syncAdapter.getUpdateEntityResponse(clazz), response);
    }

    protected OVirtClient.CompositeResponse<List<E>> getSyncAllResponse(final OVirtClient.Response<List<E>> response, String... ids) {
        return new OVirtClient.CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(clazz), response);
    }

    @Override
    public E mapFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(clazz).fromCursor(cursor);
    }

    @Override
    public void syncOne(OVirtClient.Response<E> response, String id, String... ids) {
        oVirtClient.fireRestRequest(getSyncOneRestRequest(id, ids), getSyncOneResponse(response, ids));
    }

    @Override
    public void syncAll(String... ids) {
        syncAll(null, ids);
    }

    @Override
    public void syncAll(OVirtClient.Response<List<E>> response, String... ids) {
        oVirtClient.fireRestRequest(getSyncAllRestRequest(ids), getSyncAllResponse(response, ids));
    }

    @Override
    public Collection<Trigger<E>> getAllTriggers() {
        // TriggerResolver does not have to be implemented, return an empty list as default
        return Collections.emptyList();
    }

    @Override
    public List<Trigger<E>> getTriggers(E entity, Collection<Trigger<E>> allTriggers) {
        // TriggerResolver does not have to be implemented, return an empty list as default
        return Collections.emptyList();
    }
}
