package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.VmTriggerResolver;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.ui.vms.VmDetailActivity_;

import java.util.Collection;
import java.util.List;

@EBean
public class VmFacade extends BaseEntityFacade<Vm> {

    @Bean
    VmTriggerResolver triggerResolver;

    @Bean
    OVirtClient oVirtClient;

    public VmFacade() {
        super(Vm.class);
    }

    @Override
    public Intent getDetailIntent(Vm entity, Context context) {
        Intent intent = new Intent(context, VmDetailActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(entity.getUri());
        return intent;
    }

    @Override
    public Collection<Trigger<Vm>> getAllTriggers() {
        return triggerResolver.getAllTriggers();
    }

    @Override
    public List<Trigger<Vm>> getTriggers(Vm entity, Collection<Trigger<Vm>> allTriggers) {
        return triggerResolver.getTriggers(entity, allTriggers);
    }

    @Override
    protected OVirtClient.Request<Vm> getRestRequest(String id) {
        return oVirtClient.getVmRequest(id);
    }
}
