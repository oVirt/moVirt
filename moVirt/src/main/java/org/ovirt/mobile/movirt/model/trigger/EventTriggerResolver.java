package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.mapping.EntityType;

import java.util.Collection;
import java.util.List;

/**
 * Class to resolve event triggers.
 * Created by Nika on 15.03.2015.
 */
@EBean
public class EventTriggerResolver extends BaseTriggerResolver<Event> {

    public EventTriggerResolver() {
        super(EntityType.EVENT);
    }

    @Override
    public List<Trigger<Event>> getTriggers(Event entity, Collection<Trigger<Event>> allTriggers) {
        return getTriggers(entity.getVmId(), entity.getClusterId(), allTriggers);
    }
}
