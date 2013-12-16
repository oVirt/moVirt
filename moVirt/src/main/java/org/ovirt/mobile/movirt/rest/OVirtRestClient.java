package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Rest(converters = {MappingJackson2HttpMessageConverter.class})
@Accept(MediaType.APPLICATION_JSON)
public interface OVirtRestClient {

    void setRootUrl(String rootUrl);

    RestTemplate getRestTemplate();

    @Get("/vms")
    Vms getVms();

    @Get("/vms?search={query}")
    Vms getVms(String query);

    @Post("/vms/{id}/start")
    void startVm(Action action, String id);

    @Get("/clusters")
    Clusters getClusters();
}
