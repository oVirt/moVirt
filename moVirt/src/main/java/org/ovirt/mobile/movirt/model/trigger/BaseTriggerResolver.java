package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.BaseEntity;
import org.ovirt.mobile.movirt.model.EntityType;
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
    public Collection<Trigger<E>> getAllTriggers() {
        return (Collection<Trigger<E>>) (Collection<?>) provider.query(Trigger.class)
                .where(ENTITY_TYPE, entityType.toString())
                .all();
    }

    public List<Trigger<E>> getTriggers(String entityId, String clusterId, Collection<Trigger<E>> allTriggers) {
        final List<Trigger<E>> res = new ArrayList<>();

        for (Trigger<E> trigger : allTriggers) {
            if (entityId != null && entityId.equals(trigger.getTargetId())) {
                res.add(trigger);
            }

            if (clusterId != null && clusterId.equals(trigger.getTargetId())) {
                res.add(trigger);
            }

            if (trigger.getScope() == Trigger.Scope.GLOBAL) {
                res.add(trigger);
            }
        }

        return res;
    }
}
