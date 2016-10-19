package org.ovirt.mobile.movirt.rest.client;

import org.androidannotations.rest.spring.annotations.Accept;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.RequiresAuthentication;
import org.androidannotations.rest.spring.annotations.RequiresCookie;
import org.androidannotations.rest.spring.annotations.RequiresHeader;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.annotations.SetsCookie;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.rest.VvFileHttpMessageConverter;
import org.ovirt.mobile.movirt.rest.dto.ConsoleConnectionDetails;

import static org.ovirt.mobile.movirt.rest.RestHelper.ACCEPT;
import static org.ovirt.mobile.movirt.rest.RestHelper.ACCEPT_ENCODING;
import static org.ovirt.mobile.movirt.rest.RestHelper.FILTER;
import static org.ovirt.mobile.movirt.rest.RestHelper.JSESSIONID;
import static org.ovirt.mobile.movirt.rest.RestHelper.PREFER;
import static org.ovirt.mobile.movirt.rest.RestHelper.SESSION_TTL;
import static org.ovirt.mobile.movirt.rest.RestHelper.VERSION;
import static org.ovirt.mobile.movirt.rest.VvFileHttpMessageConverter.X_VIRT_VIEWER_MEDIA_TYPE;

@Rest(converters = VvFileHttpMessageConverter.class)
@Accept(X_VIRT_VIEWER_MEDIA_TYPE)
@RequiresHeader({FILTER, ACCEPT_ENCODING, SESSION_TTL, PREFER, VERSION, ACCEPT})
@SetsCookie(JSESSIONID)
@RequiresCookie(JSESSIONID)
@RequiresAuthentication
public interface OVirtVvRestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    void setCookie(String name, String value);

    String getCookie(String name);

    @Get("/vms/{vmId}/graphicsconsoles/{consoleId}")
    ConsoleConnectionDetails getConsoleFile(@Path String vmId, @Path String consoleId);
}
