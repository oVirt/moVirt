package org.ovirt.mobile.movirt.model.trigger;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.Vm;

import java.util.HashMap;
import java.util.Map;

@EBean
public class TriggerResolverFactory {

    @Bean
    VmTriggerResolver vmTriggerResolver;

    private final Map<Class<?>, TriggerResolver<?>> resolvers = new HashMap<>();

    @AfterInject
    void initTriggerResolverMap() {
        resolvers.put(Vm.class, vmTriggerResolver);
    }

    @SuppressWarnings("unchecked")
    public <E extends OVirtEntity> TriggerResolver<E> getResolverForEntity(Class<E> clazz) {
        return (TriggerResolver<E>) resolvers.get(clazz);
    }
}
