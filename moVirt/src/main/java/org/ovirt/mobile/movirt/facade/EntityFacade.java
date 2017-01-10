package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

/**
 * Provides uniform interface for various services dependant on entity type
 *
 * @param <E> Type of entity
 */
public interface EntityFacade<E extends OVirtEntity> extends TriggerResolver<E> {

    E mapFromCursor(Cursor cursor);

    List<E> mapAllFromCursor(Cursor cursor);

    Intent getDetailIntent(E entity, Context context);

    void syncOne(Response<E> response, String id, String... ids);

    void syncAll(String... ids);

    void syncAll(Response<List<E>> response, String... ids);
}
