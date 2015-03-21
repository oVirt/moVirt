package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.BaseEntity;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Vm;

import java.util.HashMap;
import java.util.Map;

@EBean
public class TriggerResolverFactory {

    @Bean
    VmTriggerResolver vmTriggerResolver;

    @Bean
    EventTriggerResolver eventTriggerResolver;

    private final Map<Class<?>, TriggerResolver<?>> resolvers = new HashMap<>();

    @AfterInject
    void initTriggerResolverMap() {
        resolvers.put(Vm.class, vmTriggerResolver);
        resolvers.put(Event.class, eventTriggerResolver);
    }

    @SuppressWarnings("unchecked")
    public <E extends BaseEntity<?>> TriggerResolver<E> getResolverForEntity(Class<E> clazz) {
        return (TriggerResolver<E>) resolvers.get(clazz);
    }
}
