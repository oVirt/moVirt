package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.androidannotations.api.rest.RestClientSupport;
import org.ovirt.mobile.movirt.rest.v4.Token;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(converters = MappingJackson2HttpMessageConverter.class)
@Accept(MediaType.APPLICATION_JSON)
@RequiresHeader({"Filter", "Accept-Encoding"})
public interface OVirtLoginV4RestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    @Post("/sso/oauth/token?grant_type=password&scope=ovirt-app-api&username={username}&password={password}")
    @Accept(MediaType.APPLICATION_JSON)
    Token login(String username, String password);
}
