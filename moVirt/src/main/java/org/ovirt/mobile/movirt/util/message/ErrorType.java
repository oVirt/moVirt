package org.ovirt.mobile.movirt.util.message;

import android.util.Log;

/**
 * Rest errors show fancier messages
 */
public enum ErrorType {
    NORMAL(Log.ERROR),
    USER(Log.INFO),
    LOGIN(Log.INFO),
    REST(Log.ERROR);

    private final int defaultLogPriority;

    ErrorType(int defaultLogPriority) {
        this.defaultLogPriority = defaultLogPriority;
    }

    public int getDefaultLogPriority() {
        return defaultLogPriority;
    }
}
