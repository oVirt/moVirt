package org.ovirt.mobile.movirt.auth.properties.manager;

public enum OnThread {
    /**
     * Notifies listeners atomically; slower for current thread
     */
    CURRENT,
    /**
     * Notifies listeners on a new thread in the future; faster for current thread.
     */
    BACKGROUND
}
