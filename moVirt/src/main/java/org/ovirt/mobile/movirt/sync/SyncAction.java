package org.ovirt.mobile.movirt.sync;

import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;

enum SyncAction {
    DATA_CENTER(DataCenter.class),
    CLUSTER(Cluster.class),
    VM(Vm.class),
    HOST(Host.class),
    STORAGE_DOMAIN(StorageDomain.class),
    DISK(Disk.class),
    EVENT(Event.class);

    private Class<?> clazz;

    SyncAction(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static SyncAction getFirstAction() {
        return values()[0];
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public SyncAction getNextAction() {
        if (hasNextAction()) {
            return values()[ordinal() + 1];
        }
        return null;
    }

    public boolean hasNextAction() {
        return ordinal() + 1 < values().length;
    }
}
