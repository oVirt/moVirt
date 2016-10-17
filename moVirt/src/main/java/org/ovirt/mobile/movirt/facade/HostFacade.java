package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.trigger.HostTriggerResolver;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.ui.hosts.HostDetailActivity_;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.Collection;
import java.util.List;

@EBean
public class HostFacade extends BaseEntityFacade<Host> {

    @Bean
    HostTriggerResolver hostTriggerResolver;

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
    protected Request<Host> getSyncOneRestRequest(String id, String... ids) {
        ObjectUtils.requireSignature(ids);
        return oVirtClient.getHostRequest(id);
    }

    @Override
    protected Request<List<Host>> getSyncAllRestRequest(String... ids) {
        ObjectUtils.requireSignature(ids);
        return oVirtClient.getHostsRequest();
    }

    @Override
    public Collection<Trigger<Host>> getAllTriggers() {
        return hostTriggerResolver.getAllTriggers();
    }

    @Override
    public List<Trigger<Host>> getTriggers(Host entity, Collection<Trigger<Host>> allTriggers) {
        return hostTriggerResolver.getTriggers(entity, allTriggers);
    }
}
