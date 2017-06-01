package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.trigger.HostTriggerResolver;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.List;

@EBean
public class HostFacade extends BaseEntityFacade<Host> {

    @Bean
    HostTriggerResolver triggerResolver;

    public HostFacade() {
        super(Host.class);
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
    protected CompositeResponse<Host> getSyncOneResponse(Response<Host> response, String... ids) {
        return respond()
                .withTriggerResolver(triggerResolver, account)
                .triggeredActions(getIntentResolver(), MainActivityFragments.HOSTS)
                .asUpdateEntityResponse()
                .addResponse(response);
    }

    @Override
    protected Response<List<Host>> getSyncAllResponse(Response<List<Host>> response, String... ids) {
        return respond()
                .withTriggerResolver(triggerResolver, account)
                .triggeredActions(getIntentResolver(), MainActivityFragments.HOSTS)
                .asUpdateEntitiesResponse()
                .addResponse(response);
    }
}
