package org.ovirt.mobile.movirt.model.enums;

import org.ovirt.mobile.movirt.R;

public enum EventSeverity {
    NORMAL(R.drawable.log_normal),
    WARNING(R.drawable.log_warning),
    ERROR(R.drawable.log_error),
    ALERT(R.drawable.log_alert);

    private final int resource;

    EventSeverity(int resource) {
        this.resource = resource;
    }

    public int getResource() {
        return resource;
    }

    public static EventSeverity fromString(String severity) {
        try {
            return EventSeverity.valueOf(severity.toUpperCase());
        } catch (Exception e) {
            return EventSeverity.NORMAL;
        }
    }
}
