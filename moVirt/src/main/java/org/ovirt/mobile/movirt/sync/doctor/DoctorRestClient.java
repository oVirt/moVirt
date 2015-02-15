package org.ovirt.mobile.movirt.sync.doctor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.androidannotations.api.rest.RestClientSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@Rest(converters = MappingJackson2HttpMessageConverter.class, interceptors = DoctorFieldSelectHttpRequestInterceptor.class)
@Accept(MediaType.APPLICATION_JSON)
public interface DoctorRestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    @Get("/entities/vm")
    List<Vm> getVms();

    @Get("/entities/vm/{id}")
    Vm getVm(String id);

    @Get("/entities/host")
    List<Host> getHosts();

    @Get("/entities/host/{id}")
    Host getHost(String id);

    @Get("/entities/cluster")
    List<Cluster> getClusters();
}
