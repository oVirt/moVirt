package org.ovirt.mobile.movirt.rest;

import com.googlecode.androidannotations.annotations.rest.Accept;
import com.googlecode.androidannotations.annotations.rest.Get;
import com.googlecode.androidannotations.annotations.rest.Post;
import com.googlecode.androidannotations.annotations.rest.Rest;
import com.googlecode.androidannotations.api.rest.MediaType;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Rest(converters = {MappingJackson2HttpMessageConverter.class})
@Accept(MediaType.APPLICATION_JSON)
public interface OVirtClient {

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
