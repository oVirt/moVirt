package org.ovirt.mobile.movirt.model.trigger;

import org.ovirt.mobile.movirt.model.base.BaseEntity;

import java.util.Collection;
import java.util.List;

public interface TriggerResolver<E extends BaseEntity<?>> {

    Collection<Trigger<E>> getAllTriggers();

    List<Trigger<E>> getTriggers(E entity, Collection<Trigger<E>> allTriggers);
}
