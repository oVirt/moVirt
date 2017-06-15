package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.base.BaseEntity;
import org.ovirt.mobile.movirt.model.mapping.EntityType;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.ENTITY_TYPE;

@EBean
public abstract class BaseTriggerResolver<E extends BaseEntity<?>> implements TriggerResolver<E> {

    @Bean
    ProviderFacade provider;

    private final EntityType entityType;

    protected BaseTriggerResolver(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public Collection<Trigger> getAllTriggers() {
        return provider.query(Trigger.class)
                .where(ENTITY_TYPE, entityType.toString())
                .all();
    }

    public List<Trigger> getFilteredTriggers(MovirtAccount account, String remoteClusterId, String remoteEntityId, Collection<Trigger> allTriggers) {
        final List<Trigger> res = new ArrayList<>();

        for (Trigger trigger : allTriggers) {

            if (trigger.getTargetId() == null) {
                if (trigger.getClusterId() == null) {
                    if (trigger.getAccountId() == null) {
                        res.add(trigger);
                    } else if (trigger.getAccountId().equals(account == null ? null : account.getId())) {
                        res.add(trigger);
                    }
                } else if (trigger.getClusterId().equals(remoteClusterId)) {
                    res.add(trigger);
                }
            } else if (trigger.getTargetId().equals(remoteEntityId)) {
                res.add(trigger);
            }
        }

        return res;
    }
}
