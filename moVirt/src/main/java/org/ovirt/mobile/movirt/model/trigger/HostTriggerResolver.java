package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.mapping.EntityType;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.OVirtContract;

import java.util.Collection;
import java.util.List;

@EBean
public class HostTriggerResolver extends BaseTriggerResolver<Host> implements OVirtContract.Trigger {

    public HostTriggerResolver() {
        super(EntityType.HOST);
    }

    @Override
    public List<Trigger<Host>> getTriggers(Host entity, Collection<Trigger<Host>> allTriggers) {
        return getTriggers(entity.getId(), entity.getClusterId(), allTriggers);
    }
}
