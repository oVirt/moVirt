package org.ovirt.mobile.movirt.model.trigger;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.base.BaseEntity;

import java.util.Collection;
import java.util.List;

public interface TriggerResolver<E extends BaseEntity<?>> {

    Collection<Trigger> getAllTriggers();

    List<Trigger> getFilteredTriggers(MovirtAccount account, E entity, Collection<Trigger> allTriggers);
}
