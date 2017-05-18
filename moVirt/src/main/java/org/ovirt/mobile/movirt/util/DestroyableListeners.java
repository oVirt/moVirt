package org.ovirt.mobile.movirt.util;

import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;

import java.util.ArrayList;
import java.util.List;

public class DestroyableListeners {

    private final List<PropertyChangedListener> listeners = new ArrayList<>();

    private AccountPropertiesManager propertiesManager;

    public DestroyableListeners(AccountPropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    public DestroyableListeners notifyAndRegisterListener(PropertyChangedListener listener) {
        if (listener != null) {
            listeners.add(listener);
            propertiesManager.notifyAndRegisterListener(listener);
        }
        return this;
    }

    public DestroyableListeners registerListener(PropertyChangedListener listener) {
        if (listener != null) {
            listeners.add(listener);
            propertiesManager.registerListener(listener);
        }
        return this;
    }

    public void destroy() {
        try {
            for (PropertyChangedListener listener : listeners) {
                propertiesManager.removeListener(listener);
            }
        } catch (AccountDeletedException ignore) {
        }
        listeners.clear();
    }
}
