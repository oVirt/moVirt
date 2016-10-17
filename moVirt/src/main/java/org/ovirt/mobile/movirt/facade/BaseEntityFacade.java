package org.ovirt.mobile.movirt.facade;

import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.Response;
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

    @Bean
    RequestHandler requestHandler;

    private final Class<E> clazz;

    protected BaseEntityFacade(Class<E> clazz) {
        this.clazz = clazz;
    }

    protected abstract Request<E> getSyncOneRestRequest(String id, String... ids);

    protected abstract Request<List<E>> getSyncAllRestRequest(String... ids);

    protected CompositeResponse<E> getSyncOneResponse(final Response<E> response, String... ids) {
        return new CompositeResponse<>(syncAdapter.getUpdateEntityResponse(clazz), response);
    }

    protected CompositeResponse<List<E>> getSyncAllResponse(final Response<List<E>> response, String... ids) {
        return new CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(clazz), response);
    }

    @Override
    public E mapFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(clazz).fromCursor(cursor);
    }

    @Override
    public List<E> mapAllFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(clazz).listFromCursor(cursor);
    }

    @Override
    public void syncOne(Response<E> response, String id, String... ids) {
        requestHandler.fireRestRequest(getSyncOneRestRequest(id, ids), getSyncOneResponse(response, ids));
    }

    @Override
    public void syncAll(String... ids) {
        syncAll(null, ids);
    }

    @Override
    public void syncAll(Response<List<E>> response, String... ids) {
        requestHandler.fireRestRequest(getSyncAllRestRequest(ids), getSyncAllResponse(response, ids));
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
