package org.ovirt.mobile.movirt.util.message;

import android.util.Log;

/**
 * Minor errors don't get shown repeatedly
 */
public enum ErrorType {
    NORMAL(Log.ERROR, false, false, true),
    USER(Log.INFO, false, false, false),
    LOGIN(Log.INFO, true, false, false),
    REST_MINOR(Log.INFO, true, true, true),
    REST_MAJOR(Log.ERROR, true, false, true);

    private final int defaultLogPriority;
    private final boolean connectionType; // updates connection info
    private final boolean minorType; // minor errors don't flood user with errors
    private final boolean notifiable; // shows system notification

    ErrorType(int defaultLogPriority, boolean connectionType, boolean minorType, boolean notifiable) {
        this.defaultLogPriority = defaultLogPriority;
        this.connectionType = connectionType;
        this.minorType = minorType;
        this.notifiable = notifiable;
    }

    public int getDefaultLogPriority() {
        return defaultLogPriority;
    }

    public boolean isConnectionType() {
        return connectionType;
    }

    public boolean isMinorType() {
        return minorType;
    }

    public boolean isNotifiable() {
        return notifiable;
    }
}
