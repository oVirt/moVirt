package org.ovirt.mobile.movirt;


public interface Broadcasts {
    String CONNECTION_FAILURE = "org.ovirt.mobile.movirt.CONNECTION_FAILURE";
    String IN_SYNC = "org.ovirt.mobile.movirt.IN_SYNC";
    String EVENTS_IN_SYNC = "org.ovirt.mobile.movirt.EVENTS_IN_SYNC";
    String REST_REQUEST_FAILED = "org.ovirt.mobile.movirt.REST_REQUEST_FAILED";
    String NO_CONNECTION_SPEFICIED = "org.ovirt.mobile.movirt.NO_CONNECTION_SPEFICIED";


    interface Extras {
        String CONNECTION_FAILURE_REASON = "org.ovirt.mobile.movirt.CONNECTION_FAILURE_REASON";
        String SYNCING = "org.ovirt.mobile.movirt.SYNCING";
    }
}
