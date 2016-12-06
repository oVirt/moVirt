package org.ovirt.mobile.movirt.rest.client.errorhandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class LoginRedirectException extends HttpClientErrorException {

    public LoginRedirectException(HttpStatus statusCode) {
        super(statusCode);
    }

    public LoginRedirectException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }
}
