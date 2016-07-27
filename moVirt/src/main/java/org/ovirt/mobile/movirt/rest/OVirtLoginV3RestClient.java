package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
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
