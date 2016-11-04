package org.ovirt.mobile.movirt;


public interface Broadcasts {
    String ERROR_MESSAGE = "org.ovirt.mobile.movirt.ERROR_MESSAGE";
    String REST_CA_FAILURE = "org.ovirt.mobile.movirt.REST_CA_FAILURE";
    String IN_SYNC = "org.ovirt.mobile.movirt.IN_SYNC";
    String EVENTS_IN_SYNC = "org.ovirt.mobile.movirt.EVENTS_IN_SYNC";
    String NO_CONNECTION_SPEFICIED = "org.ovirt.mobile.movirt.NO_CONNECTION_SPEFICIED";
    String IN_USER_LOGIN = "org.ovirt.mobile.movirt.IN_USER_LOGIN";

    interface Extras {
        String ERROR_REASON = "org.ovirt.mobile.movirt.ERROR_REASON";
        String REPEATED_MINOR_ERROR = "org.ovirt.mobile.movirt.REPEATED_MINOR_ERROR";
        String SYNCING = "org.ovirt.mobile.movirt.SYNCING";
        String MESSAGE = "org.ovirt.mobile.movirt.MESSAGE";
    }
}
