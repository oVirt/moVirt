package org.ovirt.mobile.movirt.model;

import java.util.Arrays;
import java.util.List;

public abstract class TriggerResolver<E extends OVirtEntity> {

    public abstract List<Trigger<E>> getTriggersForId(String id);

    private static final Trigger<Vm> DUMMY_VM_TRIGGER = new Trigger<Vm>() {{
        //setCondition(new DummyVmCondition());
    }};

    private static final TriggerResolver<Vm> vmTriggerResolver = new TriggerResolver<Vm>() {
        @Override
        public List<Trigger<Vm>> getTriggersForId(String id) {
            return Arrays.asList(DUMMY_VM_TRIGGER);
        }
    };

    public static <E extends OVirtEntity> TriggerResolver<E> forEntity(Class<E> clazz) {
        if (clazz == Vm.class) {
        //    return (TriggerResolver<E>) vmTriggerResolver;
        }
        return null;
    }
}
