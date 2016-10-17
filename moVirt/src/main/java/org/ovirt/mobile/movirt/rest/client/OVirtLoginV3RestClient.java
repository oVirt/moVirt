package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.rest.spring.annotations.Accept;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.RequiresAuthentication;
import org.androidannotations.rest.spring.annotations.RequiresCookie;
import org.androidannotations.rest.spring.annotations.RequiresHeader;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.annotations.SetsCookie;
import org.androidannotations.rest.spring.api.MediaType;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.rest.dto.Api;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(converters = MappingJackson2HttpMessageConverter.class)
@Accept(MediaType.APPLICATION_JSON)
@RequiresHeader({"Filter", "Accept-Encoding", "Session-TTL", "Prefer"})
@SetsCookie("JSESSIONID")
@RequiresCookie("JSESSIONID")
@RequiresAuthentication
public interface OVirtLoginV3RestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    void setCookie(String name, String value);

    String getCookie(String name);

    @Get("/")
    Api login();

}
