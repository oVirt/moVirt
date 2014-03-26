package org.ovirt.mobile.movirt.model.trigger;

import org.ovirt.mobile.movirt.model.OVirtEntity;

import java.util.List;

public interface TriggerResolver<E extends OVirtEntity> {
    List<Trigger<E>> getTriggersForEntity(E entity);
}
