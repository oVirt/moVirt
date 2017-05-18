package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.trigger.EventTriggerResolver;
import org.ovirt.mobile.movirt.provider.EventProviderHelper;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.ui.MainActivityFragments;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.List;

@EBean
public class EventFacade extends BaseEntityFacade<Event> {

    @Bean
    EventTriggerResolver triggerResolver;

    private EventProviderHelper eventProviderHelper;

    public EventFacade() {
        super(Event.class);
    }

    public void setEventProviderHelper(EventProviderHelper eventProviderHelper) {
        this.eventProviderHelper = eventProviderHelper;
    }

    @Override
    public BaseEntityFacade<Event> init(AccountPropertiesManager propertiesManager, OVirtClient oVirtClient, RequestHandler requestHandler) {
        ObjectUtils.requireAllNotNull(eventProviderHelper);
        return super.init(propertiesManager, oVirtClient, requestHandler);
    }

    @Override
    protected Request<List<Event>> getSyncAllRestRequest(String... ids) {
        return oVirtClient.getEventsRequest();
    }

    @Override
    protected Response<List<Event>> getSyncAllResponse(Response<List<Event>> response, String... ids) {
        return respond()
                .withTriggerResolver(triggerResolver)
                .triggeredActions(null, MainActivityFragments.EVENTS)
                .doNotRemoveExpired()
                .doNotUpdateChanged()
                .checkTriggersForNewEntities()
                .asUpdateEntitiesResponse()
                .addResponse(response)
                .addResponse(new SimpleResponse<List<Event>>() {
                    @Override
                    public void after() {
                        eventProviderHelper.deleteOldEvents();
                    }
                });
    }
}
