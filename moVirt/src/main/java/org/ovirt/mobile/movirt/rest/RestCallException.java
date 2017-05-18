package org.ovirt.mobile.movirt.rest;

public class RestCallException extends Exception {

    private RestCallError restCallError;

    public RestCallException(RestCallError restCallError) {
        this.restCallError = restCallError;
    }

    public RestCallException(RestCallError restCallError, String message) {
        super(message);
        this.restCallError = restCallError;
    }

    public RestCallException(RestCallError restCallError, Throwable cause) {
        super(cause);
        this.restCallError = restCallError;
    }

    public RestCallException(RestCallError restCallError, String message, Throwable cause) {
        super(message, cause);
        this.restCallError = restCallError;
    }

    public RestCallError getCallResult() {
        return restCallError;
    }

    public boolean isRepeatable() {
        switch (restCallError) {
            // do not bother API with multiple bad requests
            case AUTH_FAILED:
            case WRONG_API_URL:
            case NO_CONNECTION: // android will interrupt if we keep stalling
                return false;
            default:
                return true;
        }
    }
}
