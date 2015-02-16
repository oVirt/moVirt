package org.ovirt.mobile.movirt;


public interface Broadcasts {
    String CONNECTION_FAILURE = "org.ovirt.mobile.movirt.CONNECTION_FAILURE";
    String IN_SYNC = "org.ovirt.mobile.movirt.IN_SYNC";
    String EVENTS_IN_SYNC = "org.ovirt.mobile.movirt.EVENTS_IN_SYNC";

    public interface Extras {
        String CONNECTION_FAILURE_REASON = "org.ovirt.mobile.movirt.CONNECTION_FAILURE_REASON";
        String SYNCING = "org.ovirt.mobile.movirt.SYNCING";
    }
}
