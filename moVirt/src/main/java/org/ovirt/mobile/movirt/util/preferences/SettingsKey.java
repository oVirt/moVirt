package org.ovirt.mobile.movirt.util.preferences;

import java.util.HashMap;
import java.util.Map;

public enum SettingsKey {
    CONNECTION_NOTIFICATION("connection_notification"),
    POLL_EVENTS("poll_events"),
    PERIODIC_SYNC("periodic_sync"),

    PERIODIC_SYNC_INTERVAL("periodic_sync_interval"),
    MAX_EVENTS_POLLED("max_events_polled"),
    MAX_EVENTS_STORED("max_events_stored"),
    MAX_VMS("max_vms_polled"),

    EVENTS_SEARCH_QUERY("events_search_query"),
    VMS_SEARCH_QUERY("vms_search_query");

    static Map<String, SettingsKey> map = new HashMap<>(values().length);

    static {
        for (SettingsKey key : values()) {
            map.put(key.getValue(), key);
        }
    }

    String value;

    SettingsKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SettingsKey from(String settingsKey) {
        return map.get(settingsKey);
    }
}
