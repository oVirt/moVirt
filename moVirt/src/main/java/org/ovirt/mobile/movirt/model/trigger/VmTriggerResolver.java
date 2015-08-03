package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.OVirtContract;

import java.util.Collection;
import java.util.List;

@EBean
public class VmTriggerResolver extends BaseTriggerResolver<Vm> implements OVirtContract.Trigger {

    public VmTriggerResolver() {
        super(EntityType.VM);
    }

    @Override
    public List<Trigger<Vm>> getTriggers(Vm entity, Collection<Trigger<Vm>> allTriggers) {
        return getTriggers(entity.getId(), entity.getClusterId(), allTriggers);
    }
}
