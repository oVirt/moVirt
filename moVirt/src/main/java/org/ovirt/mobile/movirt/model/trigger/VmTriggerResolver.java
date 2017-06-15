package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.mapping.EntityType;
import org.ovirt.mobile.movirt.provider.OVirtContract;

import java.util.Collection;
import java.util.List;

@EBean
public class VmTriggerResolver extends BaseTriggerResolver<Vm> implements OVirtContract.Trigger {

    public VmTriggerResolver() {
        super(EntityType.VM);
    }

    @Override
    public List<Trigger> getFilteredTriggers(MovirtAccount account, Vm entity, Collection<Trigger> allTriggers) {
        return getFilteredTriggers(account, entity.getClusterId(), entity.getId(), allTriggers);
    }
}
