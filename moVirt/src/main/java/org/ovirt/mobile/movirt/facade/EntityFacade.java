package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.trigger.TriggerResolver;
import org.ovirt.mobile.movirt.sync.OVirtClient;
import org.ovirt.mobile.movirt.sync.Response;

/**
 * Provides uniform interface for various services dependant on entity type
 * @param <E> Type of entity
 */
public interface EntityFacade<E extends OVirtEntity> extends TriggerResolver<E> {

    E mapFromCursor(Cursor cursor);

    Intent getDetailIntent(E entity, Context context);

    void sync(String id, Response<E> response);
}
