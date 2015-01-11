package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientErrorHandling;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.androidannotations.api.rest.RestClientSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(converters = MappingJackson2HttpMessageConverter.class)
@Accept(MediaType.APPLICATION_JSON)
@RequiresAuthentication
@RequiresHeader("Filter")
public interface OVirtRestClient extends RestClientRootUrl, RestClientHeaders, RestClientErrorHandling, RestClientSupport {

    @Get("/vms")
    Vms getVms();

    @Get("/vms?search={query}")
    Vms getVms(String query);

    @Get("/vms/{id}/statistics")
    Statistics getVmStatistics(String id);
    @Post("/vms/{id}/start")
    void startVm(Action action, String id);

    @Post("/vms/{id}/stop")
    void stopVm(Action action, String id);

    @Post("/vms/{id}/reboot")
    void rebootVm(Action action, String id);

    @Get("/clusters")
    Clusters getClusters();

    @Get("/events;max={maxToLoad}?from={lastEventId}")
    Events getEventsSince(String lastEventId, int maxToLoad);

}
