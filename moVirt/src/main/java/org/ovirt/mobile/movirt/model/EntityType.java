package org.ovirt.mobile.movirt.model;

public enum EntityType {
    VM(Vm.class),
    CLUSTER(Cluster.class),
    EVENT(Event.class);

    private final Class<? extends BaseEntity<?>> entityClass;

    EntityType(Class<? extends BaseEntity<?>> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<? extends BaseEntity<?>> getEntityClass() {
        return entityClass;
    }
}
