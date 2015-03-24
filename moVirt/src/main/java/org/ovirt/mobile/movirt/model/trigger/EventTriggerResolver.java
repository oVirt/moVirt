package org.ovirt.mobile.movirt.model.trigger;

import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.SCOPE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.TARGET_ID;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.ENTITY_TYPE;

/**
 * Class to resolve event triggers.
 * Created by Nika on 15.03.2015.
 */
@EBean
public class EventTriggerResolver implements TriggerResolver<Event> {

    private static final String TAG = EventTriggerResolver.class.getSimpleName();

    @Bean
    ProviderFacade provider;

    @Override
    public List<Trigger<Event>> getTriggersForEntity(Event event) {
        final List<Trigger<Event>> triggers = new ArrayList<>();
        try {
            if (event.getVmId() != null){
                triggers.addAll(this.getVmTriggers(event));
            }
            if (event.getClusterId() != null ) {
                triggers.addAll(this.getClusterTriggers(event));
            }
            triggers.addAll(this.getGlobalTriggers());
            return triggers;
        } catch (RuntimeException e) {
            Log.e(TAG, "Error resolving triggers for event: ", e);
            return Collections.emptyList();
        }
    }

    private Collection<Trigger<Event>> getVmTriggers(Event event) {
        return (Collection<Trigger<Event>>) (Collection<?>) provider.query(Trigger.class)
                .where(TARGET_ID, event.getVmId())
                .where(ENTITY_TYPE, "EVENT")
                .all();
    }

    private Collection<Trigger<Event>> getClusterTriggers(Event event) {
        return (Collection<Trigger<Event>>) (Collection<?>) provider.query(Trigger.class)
                .where(TARGET_ID, event.getClusterId())
                .where(ENTITY_TYPE, "EVENT")
                .all();
    }

    private Collection<Trigger<Event>> getGlobalTriggers() {
        return (Collection<Trigger<Event>>) (Collection<?>) provider.query(Trigger.class)
                .where(SCOPE, Trigger.Scope.GLOBAL.toString())
                .where(ENTITY_TYPE, "EVENT")
                .all();
    }

}
