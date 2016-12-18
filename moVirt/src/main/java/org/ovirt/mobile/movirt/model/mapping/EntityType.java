package org.ovirt.mobile.movirt.model.mapping;

import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.base.BaseEntity;

public enum EntityType {
    VM(Vm.class),
    CLUSTER(Cluster.class),
    HOST(Host.class),
    STORAGE_DOMAIN(StorageDomain.class),
    EVENT(Event.class);

    private final Class<? extends BaseEntity<?>> entityClass;

    EntityType(Class<? extends BaseEntity<?>> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<? extends BaseEntity<?>> getEntityClass() {
        return entityClass;
    }
}
