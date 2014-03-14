package org.ovirt.mobile.movirt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TriggerResolver<E extends OVirtEntity> {

    public abstract List<Trigger<E>> getTriggersForId(String id);

    private static final Map<Class<?>, TriggerResolver<?>> resolvers = new HashMap<>();

    private static final TriggerResolver<Vm> VM_RESOLVER = new TriggerResolver<Vm>() {
        @Override
        public List<Trigger<Vm>> getTriggersForId(String id) {
            // get vm specific triggers
            // get cluster triggers
            // get global triggers
            return new ArrayList<>();
        }
    };

    static {
        resolvers.put(Vm.class, VM_RESOLVER);
    }

    @SuppressWarnings("unchecked")
    public static <E extends OVirtEntity> TriggerResolver<E> forEntity(Class<E> clazz) {
        return (TriggerResolver<E>) resolvers.get(clazz);
    }
}
