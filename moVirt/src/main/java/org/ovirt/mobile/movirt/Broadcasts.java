package org.ovirt.mobile.movirt;

import static org.ovirt.mobile.movirt.Constants.APP_PACKAGE_DOT;

public interface Broadcasts {
    String ERROR_MESSAGE = APP_PACKAGE_DOT + "ERROR_MESSAGE";
    String REST_CA_FAILURE = APP_PACKAGE_DOT + "REST_CA_FAILURE";

    interface Extras {
        String ERROR_HEADER = APP_PACKAGE_DOT + "ERROR_HEADER";
        String ERROR_REASON = APP_PACKAGE_DOT + "ERROR_REASON";
        String ERROR_API_URL = APP_PACKAGE_DOT + "ERROR_API_URL";
        String ERROR_ACCOUNT = APP_PACKAGE_DOT + "ERROR_ACCOUNT";
    }
}
