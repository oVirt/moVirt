package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;

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

    private final Map<Class<?>, EntityFacade<?>> facades = new HashMap<>();

    @AfterInject
    void initFacadeMap() {
        addFacade(Vm.class, vmFacade);
        addFacade(Host.class, hostFacade);
        addFacade(StorageDomain.class, storageDomainFacade);
    }

    private <E extends OVirtEntity> void addFacade(Class<E> clazz, EntityFacade<E> facade) {
        facades.put(clazz, facade);
    }

    @SuppressWarnings("unchecked")
    public <E extends OVirtEntity> EntityFacade<E> getFacade(Class<E> clazz) {
        return (EntityFacade<E>) facades.get(clazz);
    }
}
