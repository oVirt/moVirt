package org.ovirt.mobile.movirt.model.trigger;

import android.content.ContentProviderClient;

import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.Vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TriggerResolver<E extends OVirtEntity> {
    List<Trigger<E>> getTriggersForEntity(E entity);
}
