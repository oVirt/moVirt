package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.RequiresCookie;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.annotations.rest.SetsCookie;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.androidannotations.api.rest.RestClientSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(converters = MappingJackson2HttpMessageConverter.class)
@Accept(MediaType.APPLICATION_JSON + "; detail=statistics")
@RequiresHeader({"Filter", "Accept-Encoding", "Session-TTL", "Prefer"})
@SetsCookie("JSESSIONID")
@RequiresCookie("JSESSIONID")
@RequiresAuthentication
public interface OVirtRestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    @Get("/vms;max={maxToLoad}")
    Vms getVms(int maxToLoad);

    @Get("/vms;max={maxToLoad}?search={query}")
    Vms getVms(String query, int maxToLoad);

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

    @Get("/events;max={maxToLoad}?from={lastEventId}&search=sortby time desc")
    Events getEventsSince(String lastEventId, int maxToLoad);

    @Get("/events;max={maxToLoad}?from={lastEventId}&search={query}")
    Events getEventsSince(String lastEventId, String query, int maxToLoad);

    @Get("/vms/{id}")
    Vm getVm(String id);

    @Post("/vms/{id}/ticket")
    ActionTicket getConsoleTicket(Action action, String id);

    @Get("/vms/{id}/disks")
    Disks getDisks(String id);

    @Get("/vms/{id}/nics")
    Nics getNics(String id);

    @Get("/hosts")
    Hosts getHosts();

    @Get("/hosts/{id}")
    Host getHost(String id);

    @Get("/")
    EmptyResult login();

    void setCookie(String name, String value);

    String getCookie(String name);

}
