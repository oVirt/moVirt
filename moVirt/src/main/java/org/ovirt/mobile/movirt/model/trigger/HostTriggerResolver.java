package org.ovirt.mobile.movirt.model.trigger;

import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityType;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EBean
public class HostTriggerResolver implements TriggerResolver<Host>, OVirtContract.Trigger {

    private static final String TAG = HostTriggerResolver.class.getSimpleName();

    @Bean
    ProviderFacade provider;

    @Override
    public List<Trigger<Host>> getTriggers(Host host) {
        final ArrayList<Trigger<Host>> triggers = new ArrayList<>();
        try {
            triggers.addAll(getHostTriggers(host));
            triggers.addAll(getClusterTriggers(host));
            triggers.addAll(getGlobalTriggers());
            return triggers;
        } catch (RuntimeException e) {
            Log.e(TAG, "Error resolving triggers for host: " + host.getId(), e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Trigger<Host>> getHostTriggers(Host host) {
        return (Collection<Trigger<Host>>) (Collection<?>) provider
                .query(Trigger.class)
                .where(TARGET_ID, host.getId())
                .where(ENTITY_TYPE, EntityType.HOST.toString())
                .all();
    }

    @SuppressWarnings("unchecked")
    private Collection<Trigger<Host>> getClusterTriggers(Host host) {
        return (Collection<Trigger<Host>>) (Collection<?>) provider
                .query(Trigger.class)
                .where(TARGET_ID, host.getClusterId())
                .where(ENTITY_TYPE, EntityType.HOST.toString())
                .all();
    }

    @SuppressWarnings("unchecked")
    private Collection<Trigger<Host>> getGlobalTriggers() {
        return (Collection<Trigger<Host>>) (Collection<?>) provider
                .query(Trigger.class)
                .where(SCOPE, Trigger.Scope.GLOBAL.toString())
                .where(ENTITY_TYPE, EntityType.HOST.toString())
                .all();
    }
}
