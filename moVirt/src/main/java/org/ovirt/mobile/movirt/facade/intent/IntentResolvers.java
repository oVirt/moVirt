package org.ovirt.mobile.movirt.facade.intent;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;

import java.util.HashMap;
import java.util.Map;

@EBean(scope = EBean.Scope.Singleton)
public class IntentResolvers {

    private final Map<Class<?>, EntityIntentResolver<?>> resolvers = new HashMap<>();

    @Bean
    StorageDomainIntentResolver storageDomainIntentResolver;

    @Bean
    HostIntentResolver hostIntentResolver;

    @Bean
    VmIntentResolver vmIntentResolver;

    @Bean
    SnapshotIntentResolver snapshotIntentResolver;

    @AfterInject
    void init() {
        resolvers.put(StorageDomain.class, storageDomainIntentResolver);
        resolvers.put(Host.class, hostIntentResolver);
        resolvers.put(Vm.class, vmIntentResolver);
        resolvers.put(Snapshot.class, snapshotIntentResolver);
    }

    @SuppressWarnings("unchecked")
    public <E extends OVirtEntity> EntityIntentResolver<E> getResolver(Class<?> clazz) {
        return (EntityIntentResolver<E>) resolvers.get(clazz);
    }
}
