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

import static org.ovirt.mobile.movirt.rest.RestHelper.ACCEPT_ENCODING;
import static org.ovirt.mobile.movirt.rest.RestHelper.JSESSIONID;
import static org.ovirt.mobile.movirt.rest.RestHelper.PREFER;
import static org.ovirt.mobile.movirt.rest.RestHelper.SESSION_TTL;

@Rest(converters = MappingJackson2HttpMessageConverter.class)
@Accept(MediaType.APPLICATION_JSON)
@RequiresHeader({ACCEPT_ENCODING, SESSION_TTL, PREFER})
@SetsCookie(JSESSIONID)
@RequiresCookie(JSESSIONID)
@RequiresAuthentication
public interface OVirtLoginV3RestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    void setCookie(String name, String value);

    String getCookie(String name);

    @Get("/")
    Api login();

}
