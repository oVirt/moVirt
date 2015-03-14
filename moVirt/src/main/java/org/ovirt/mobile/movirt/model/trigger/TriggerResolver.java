package org.ovirt.mobile.movirt.model.trigger;

import org.ovirt.mobile.movirt.model.BaseEntity;

import java.util.List;

public interface TriggerResolver<E extends BaseEntity<?>> {
    List<Trigger<E>> getTriggers(E entity);
}
