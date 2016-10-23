package org.ovirt.mobile.movirt.util.message;

import android.util.Log;

/**
 * Minor errors don't get shown repeatedly
 */
public enum ErrorType {
    NORMAL(Log.ERROR, false, false),
    USER(Log.INFO, false, false),
    LOGIN(Log.INFO, true, false),
    REST_MINOR(Log.INFO, true, true),
    REST_MAJOR(Log.ERROR, true, false);

    private int defaultLogPriority;
    private boolean connectionType;
    private boolean minorType;

    ErrorType(int defaultLogPriority, boolean connectionType, boolean minorType) {
        this.defaultLogPriority = defaultLogPriority;
        this.connectionType = connectionType;
        this.minorType = minorType;
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
}
