package org.ovirt.mobile.movirt;


public interface Broadcasts {
    String CONNECTION_FAILURE = "org.ovirt.mobile.movirt.CONNECTION_FAILURE";
    String LOGIN_FAILURE = "org.ovirt.mobile.movirt.LOGIN_FAILURE";
    String REST_CA_FAILURE = "org.ovirt.mobile.movirt.REST_CA_FAILURE";
    String IN_SYNC = "org.ovirt.mobile.movirt.IN_SYNC";
    String EVENTS_IN_SYNC = "org.ovirt.mobile.movirt.EVENTS_IN_SYNC";
    String NO_CONNECTION_SPEFICIED = "org.ovirt.mobile.movirt.NO_CONNECTION_SPEFICIED";

    interface Extras {
        String FAILURE_REASON = "org.ovirt.mobile.movirt.FAILURE_REASON";
        String REPEATED_CONNECTION_FAILURE = "org.ovirt.mobile.movirt.REPEATED_CONNECTION_FAILURE";
        String SYNCING = "org.ovirt.mobile.movirt.SYNCING";
    }
}
