package org.ovirt.mobile.movirt.model.trigger;

import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.SCOPE;
import static org.ovirt.mobile.movirt.provider.OVirtContract.Trigger.TARGET_ID;

@EBean
public class VmTriggerResolver implements TriggerResolver<Vm> {

    private static final String TAG = VmTriggerResolver.class.getSimpleName();

    @Bean
    ProviderFacade provider;

    @Override
    public List<Trigger<Vm>> getTriggersForEntity(Vm vm) {
        final ArrayList<Trigger<Vm>> triggers = new ArrayList<>();
        try {
            triggers.addAll(getVmTriggers(vm));
            triggers.addAll(getClusterTriggers(vm));
            triggers.addAll(getGlobalTriggers());
            return triggers;
        } catch (RuntimeException e) {
            Log.e(TAG, "Error resolving triggers for vm: " + vm.getId(), e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Trigger<Vm>> getVmTriggers(Vm vm) {
        return (Collection<Trigger<Vm>>) (Collection<?>) provider.query(Trigger.class).where(TARGET_ID, vm.getId()).all();
    }

    @SuppressWarnings("unchecked")
    private Collection<Trigger<Vm>> getClusterTriggers(Vm vm) {
        return (Collection<Trigger<Vm>>) (Collection<?>) provider.query(Trigger.class).where(TARGET_ID, vm.getClusterId()).all();
    }

    @SuppressWarnings("unchecked")
    private Collection<Trigger<Vm>> getGlobalTriggers() {
        return (Collection<Trigger<Vm>>) (Collection<?>) provider.query(Trigger.class).where(SCOPE, Trigger.Scope.GLOBAL.toString()).all();
    }
}
