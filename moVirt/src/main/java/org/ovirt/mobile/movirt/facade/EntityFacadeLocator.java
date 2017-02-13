package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Console;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.DiskAttachment;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.SnapshotDisk;
import org.ovirt.mobile.movirt.model.SnapshotNic;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;

import java.util.HashMap;
import java.util.Map;

@EBean(scope = EBean.Scope.Singleton)
public class EntityFacadeLocator {

    @Bean
    VmFacade vmFacade;

    @Bean
    HostFacade hostFacade;

    @Bean
    StorageDomainFacade storageDomainFacade;

    @Bean
    DiskFacade diskFacade;

    @Bean
    DiskAttachmentsFacade diskAttachmentsFacade;

    @Bean
    NicFacade nicFacade;

    @Bean
    SnapshotFacade snapshotFacade;

    @Bean
    SnapshotDiskFacade snapshotDiskFacade;

    @Bean
    SnapshotNicFacade snapshotNicFacade;

    @Bean
    ConsoleFacade consoleFacade;

    private final Map<Class<?>, EntityFacade<?>> facades = new HashMap<>();

    @AfterInject
    void initFacadeMap() {
        addFacade(Vm.class, vmFacade);
        addFacade(Host.class, hostFacade);
        addFacade(StorageDomain.class, storageDomainFacade);
        addFacade(Disk.class, diskFacade);
        addFacade(DiskAttachment.class, diskAttachmentsFacade);
        addFacade(Nic.class, nicFacade);
        addFacade(Snapshot.class, snapshotFacade);
        addFacade(SnapshotDisk.class, snapshotDiskFacade);
        addFacade(SnapshotNic.class, snapshotNicFacade);
        addFacade(Console.class, consoleFacade);
    }

    private <E extends OVirtEntity> void addFacade(Class<E> clazz, EntityFacade<E> facade) {
        facades.put(clazz, facade);
    }

    @SuppressWarnings("unchecked")
    public <E extends OVirtEntity> EntityFacade<E> getFacade(Class<E> clazz) {
        return (EntityFacade<E>) facades.get(clazz);
    }
}
