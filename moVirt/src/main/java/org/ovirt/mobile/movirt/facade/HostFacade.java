package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.trigger.HostTriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.ui.hosts.HostDetailActivity_;

import java.util.Collection;
import java.util.List;

@EBean
public class HostFacade extends BaseEntityFacade<Host> {

    @Bean
    HostTriggerResolver hostTriggerResolver;

    @Bean
    OVirtClient oVirtClient;

    public HostFacade() {
        super(Host.class);
    }

    @Override
    public Intent getDetailIntent(Host entity, Context context) {
        Intent intent = new Intent(context, HostDetailActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(entity.getUri());
        return intent;
    }

    @Override
    public Collection<Trigger<Host>> getAllTriggers() {
        return hostTriggerResolver.getAllTriggers();
    }

    @Override
    public List<Trigger<Host>> getTriggers(Host entity, Collection<Trigger<Host>> allTriggers) {
        return hostTriggerResolver.getTriggers(entity, allTriggers);
    }

    @Override
    protected OVirtClient.Request<Host> getRestRequest(String id) {
        return oVirtClient.getHostRequest(id);
    }
}
