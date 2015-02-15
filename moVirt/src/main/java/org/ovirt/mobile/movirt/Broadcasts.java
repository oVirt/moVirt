package org.ovirt.mobile.movirt;


public interface Broadcasts {
    String CONNECTION_FAILURE = "org.ovirt.mobile.movirt.CONNECTION_FAILURE";
    String IN_SYNC = "org.ovirt.mobile.movirt.IN_SYNC";
    String EVENTS_IN_SYNC = "org.ovirt.mobile.movirt.EVENTS_IN_SYNC";
    String REST_REQUEST_FAILED = "org.ovirt.mobile.movirt.REST_REQUEST_FAILED";
    String NO_CONNECTION_SPEFICIED = "org.ovirt.mobile.movirt.NO_CONNECTION_SPEFICIED";

    String REFRESH_TRIGGERED = "org.ovirt.mobile.movirt.REFRESH_TRIGGERED";

    // push notifications
    String MQTT_CONNECTED = "org.ovirt.mobile.movirt.MQTT_CONNECTED";
    String MQTT_DISCONNECTED = "org.ovirt.mobile.movirt.MQTT_DISCONNECTED";
    String VMS_UPDATED = "org.ovirt.mobile.movirt.VMS_UPDATED";
    String HOSTS_UPDATED = "org.ovirt.mobile.movirt.HOSTS_UPDATED";
    String EVENTS_UPDATED = "org.ovirt.mobile.movirt.EVENTS_UPDATED";

    public interface Extras {
        String CONNECTION_FAILURE_REASON = "org.ovirt.mobile.movirt.CONNECTION_FAILURE_REASON";
        String SYNCING = "org.ovirt.mobile.movirt.SYNCING";
        String ID = "org.ovirt.mobile.movirt.ID";
        String CHANGED_FIELDS = "org.ovirt.mobile.movirt.CHANGED_FIELDS";
    }
}
