package org.ovirt.mobile.movirt.rest.client.errorhandler;

import android.content.Context;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.util.URIUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@EBean
public class LoginErrorHandler extends DefaultResponseErrorHandler {
    @RootContext
    Context context;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return super.hasError(response) || response.getStatusCode().series() == HttpStatus.Series.REDIRECTION;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus code = response.getStatusCode();
        switch (code.series()) {
            case REDIRECTION:
                URI location = URIUtils.getURIWithoutParameters(response.getHeaders().getLocation());
                String message = context.getString(R.string.login_server_send_redirect,
                        (location == null ? "" : location.toString()));
                throw new LoginRedirectException(code, message);
            default:
                super.handleError(response);
                break;
        }
    }
}
