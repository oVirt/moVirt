package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.mapping.EntityType;
import org.ovirt.mobile.movirt.provider.OVirtContract;

import java.util.Collection;
import java.util.List;

@EBean
/**
 * Host triggers still not supported (https://github.com/oVirt/moVirt/issues/63)
 */
public class HostTriggerResolver extends BaseTriggerResolver<Host> implements OVirtContract.Trigger {

    public HostTriggerResolver() {
        super(EntityType.HOST);
    }

    @Override
    public List<Trigger> getFilteredTriggers(MovirtAccount account, Host entity, Collection<Trigger> allTriggers) {
        return getFilteredTriggers(account, entity.getClusterId(), entity.getId(), allTriggers);
    }
}
