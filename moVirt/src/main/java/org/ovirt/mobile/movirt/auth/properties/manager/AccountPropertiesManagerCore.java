package org.ovirt.mobile.movirt.auth.properties.manager;

import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.auth.properties.PropertyUtils;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Only methods registerListener, notifyAndRegisterListener and notifyListener are safe to use from @AfterInject.
 * Other methods are NOT SAFE to call! Call isInitialized() for checking the state of this class.
 * It is not needed to check isInitialized() after all of the classes have been initialized.
 */
abstract class AccountPropertiesManagerCore {
    private static final String TAG = AccountPropertiesManagerCore.class.getSimpleName();

    private static final String PROPERTY = "property";
    private static final String LISTENER = "listener";

    // this is not guaranteed to be set (circular injection) when this class has been already injected somewhere else
    protected MovirtAuthenticator authenticator;

    private boolean initialized = false;

    private static final Map<AccountProperty, Set<WrappedPropertyChangedListener>> listeners;
    private static Map<AccountProperty, Set<WrappedPropertyChangedListener>> initQueueListeners;

    static {
        listeners = new EnumMap<>(AccountProperty.class);
        initQueueListeners = new EnumMap<>(AccountProperty.class);

        for (AccountProperty property : AccountProperty.values()) {
            listeners.put(property, Collections.synchronizedSet(new HashSet<WrappedPropertyChangedListener>()));
            initQueueListeners.put(property, Collections.synchronizedSet(new HashSet<WrappedPropertyChangedListener>()));
        }
    }

    synchronized void setAuthenticator(MovirtAuthenticator authenticator) { // in very improbable case of multiple instances, AccountPropertiesManager is singleton
        if (this.authenticator != null) {
            return;
        }

        this.authenticator = authenticator;
        initialized = true;

        for (AccountProperty property : AccountProperty.values()) {
            notifyListeners(initQueueListeners.get(property), authenticator.getResource(property)); // should be run in current thread
        }
        initQueueListeners = null; // free
    }

    /**
     * Must be checked before any method (of this class) from @AfterInject of other classes, except for methods registerListener and
     * notifyAndRegisterListener or notifyListener which will be invoked the first time this component is initialized.
     * <p>
     * Other methods are NOT SAFE to call from @AfterInject!
     *
     * @return true if this component is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Calls
     * {@link AccountPropertiesManagerCore#notifyListener(PropertyChangedListener)} and
     * {@link AccountPropertiesManagerCore#registerListener(PropertyChangedListener)}
     */
    public <E> void notifyAndRegisterListener(final PropertyChangedListener<E> listener) {
        notifyListener(listener);
        registerListener(listener);
    }

    /**
     * Notifies listener
     * The listener IS GUARANTEED to be called from current thread, UNLESS caller of this method created new thread in @AfterViews.
     *
     * @param listener listens for changes of property defined in {@link PropertyChangedListener#getProperty() getProperty}.
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@link PropertyChangedListener#getProperty() getProperty}.
     */
    @SuppressWarnings("unchecked")
    public <E> void notifyListener(final PropertyChangedListener<E> listener) {
        if (isInitialized()) {
            ObjectUtils.requireNotNull(listener.getProperty(), PROPERTY);
            ObjectUtils.requireNotNull(listener, LISTENER);
            listener.onPropertyChange((E) authenticator.getResource(listener.getProperty()));
        } else {
            registerListenerImpl(initQueueListeners, listener);
        }
    }

    /**
     * Registers listener.
     * The listener IS NOT GUARANTEED to be called from the main UI thread (listener will be called from
     * a thread specified by a caller of a setter of the property)
     *
     * @param listener listens for changes of property defined in {@link PropertyChangedListener#getProperty() getProperty}
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@link PropertyChangedListener#getProperty() getProperty}
     * @see OnThread
     */
    public <E> void registerListener(final PropertyChangedListener<E> listener) {
        registerListenerImpl(listeners, listener);
    }

    /**
     * @param listener to be removed from this manager
     * @return true if removed
     */
    public boolean removeListener(final PropertyChangedListener listener) {
        if (listener == null) {
            return false;
        }

        WrappedPropertyChangedListener toRemove = new WrappedPropertyChangedListener() {
            @Override
            void onPropertyChange(Object o) {
            }

            @NonNull
            @Override
            PropertyChangedListener getListener() {
                return listener;
            }
        };
        boolean result = false;

        for (Set<WrappedPropertyChangedListener> propertyListeners : listeners.values()) {
            result = propertyListeners.remove(toRemove) || result;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <E> void registerListenerImpl(Map<AccountProperty, Set<WrappedPropertyChangedListener>> listeners,
                                          final PropertyChangedListener<E> listener) {
        ObjectUtils.requireNotNull(listener.getProperty(), PROPERTY);
        ObjectUtils.requireNotNull(listener, LISTENER);

        listeners.get(listener.getProperty()).add(new WrappedPropertyChangedListener() {
            @Override
            public void onPropertyChange(Object newProperty) {
                listener.onPropertyChange((E) newProperty);
            }

            @NonNull
            @Override
            PropertyChangedListener getListener() {
                return listener;
            }
        });
    }

    /**
     * @param property property to be checked against
     * @param object   data to be checked against
     * @return true if property state is different than object
     */
    public boolean propertyDiffers(AccountProperty property, Object object) {
        ObjectUtils.requireNotNull(property, PROPERTY);

        Object old = authenticator.getResource(property);
        return !PropertyUtils.propertyObjectEquals(old, object);
    }

    /**
     * @param property    to be set and notified
     * @param object      data to be set
     * @param runOnThread thread to fire the listeners on
     * @return true if property state changed and listeners were notified
     */
    public boolean setAndNotify(AccountProperty property, Object object, OnThread runOnThread) {
        boolean propertyChanged = propertyDiffers(property, object);
        if (propertyChanged) {
            authenticator.setResource(property, object);
            if (!propertyDiffers(property, object)) { // setter worked
                notifyListeners(property, authenticator.getResource(property), runOnThread); // get set value
                notifyDependentProperties(property, runOnThread);
            } else {
                throw new IllegalStateException("Setter of account property " + property.name() + " doesn't set anything!");
            }
        }
        return propertyChanged;
    }

    private void notifyDependentProperties(AccountProperty property, OnThread runOnThread) {
        for (AccountProperty prop : property.getDependentProperties()) {
            notifyListeners(prop, authenticator.getResource(prop), runOnThread);
        }
    }

    private void notifyListeners(AccountProperty property, Object o, OnThread runOnThread) {
        Set<WrappedPropertyChangedListener> propertyListeners = listeners.get(property);
        switch (runOnThread) {
            case CURRENT:
                notifyListeners(propertyListeners, o);
                break;
            case BACKGROUND:
                notifyBackgroundListeners(propertyListeners, o);
                break;
        }
    }

    void notifyListeners(Set<WrappedPropertyChangedListener> currentListeners, Object o) {
        if (currentListeners.isEmpty()) {
            return;
        }

        synchronized (currentListeners) {
            for (WrappedPropertyChangedListener listener : currentListeners) {
                listener.onPropertyChange(o);
            }
        }
    }

    /**
     * This method should not be used outside of {@link AccountPropertiesManager}
     */
    abstract void notifyBackgroundListeners(Set<WrappedPropertyChangedListener> backgroundListeners, Object o);

    abstract class WrappedPropertyChangedListener {
        abstract void onPropertyChange(Object o);

        @NonNull
        abstract PropertyChangedListener getListener();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WrappedPropertyChangedListener)) return false;

            WrappedPropertyChangedListener that = (WrappedPropertyChangedListener) o;

            return getListener().equals(that.getListener());
        }

        @Override
        public int hashCode() {
            return getListener().hashCode();
        }
    }
}
