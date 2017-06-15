package org.ovirt.mobile.movirt.facade;

import android.database.Cursor;

import org.ovirt.mobile.movirt.facade.intent.EntityIntentResolver;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.RestCallException;

import java.util.List;

/**
 * Provides uniform interface for various services dependant on entity type
 *
 * @param <E> Type of entity
 */
public interface EntityFacade<E extends OVirtEntity> {

    E mapFromCursor(Cursor cursor);

    List<E> mapAllFromCursor(Cursor cursor);

    EntityIntentResolver<E> getIntentResolver();

    void syncOne(Response<E> response, String id, String... ids);

    void syncAll(String... ids);

    void syncAll(Response<List<E>> response, String... ids);

    void syncAllUnsafe(String... ids) throws RestCallException;

    void syncAllUnsafe(Response<List<E>> response, String... ids) throws RestCallException;
}
