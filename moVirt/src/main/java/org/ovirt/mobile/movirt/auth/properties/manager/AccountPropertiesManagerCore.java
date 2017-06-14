package org.ovirt.mobile.movirt.auth.properties.manager;

import android.support.annotation.NonNull;

import org.androidannotations.api.BackgroundExecutor;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.AccountPropertiesRW;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.auth.properties.PropertyUtils;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class AccountPropertiesManagerCore implements AccountEnvironment.EnvDisposable {

    private static final String PROPERTY = "property";
    private static final String LISTENER = "listener";

    protected AccountPropertiesRW accountPropertiesRW;

    private final Map<AccountProperty, Set<WrappedPropertyChangedListener>> listeners = new EnumMap<>(AccountProperty.class);

    public AccountPropertiesManagerCore(AccountPropertiesRW accountPropertiesRW) {
        ObjectUtils.requireNotNull(accountPropertiesRW, "accountPropertiesRW");
        this.accountPropertiesRW = accountPropertiesRW;

        for (AccountProperty property : AccountProperty.values()) {
            listeners.put(property, Collections.synchronizedSet(new HashSet<>()));
        }
    }

    @Override
    public void dispose() {
        for (Set<WrappedPropertyChangedListener> propertyListeners : listeners.values()) {
            propertyListeners.clear();
        }
        listeners.clear();
        accountPropertiesRW.destroy();
    }

    public MovirtAccount getManagedAccount() {
        return accountPropertiesRW.getAccount();
    }

    /**
     * Calls
     * {@link AccountPropertiesManagerCore#notifyListener(PropertyChangedListener)} and
     * {@link AccountPropertiesManagerCore#registerListener(PropertyChangedListener)}
     */
    public <E> void notifyAndRegisterListener(final PropertyChangedListener<E> listener) throws AccountDeletedException {
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
    public <E> void notifyListener(final PropertyChangedListener<E> listener) throws AccountDeletedException {
        ObjectUtils.requireNotNull(listener.getProperty(), PROPERTY);
        ObjectUtils.requireNotNull(listener, LISTENER);
        listener.onPropertyChange((E) accountPropertiesRW.getResource(listener.getProperty()));
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
    public <E> void registerListener(final PropertyChangedListener<E> listener) throws AccountDeletedException {
        if (accountPropertiesRW.isDestroyed()) {
            throw new AccountDeletedException();
        }
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
    public boolean propertyDiffers(AccountProperty property, Object object) throws AccountDeletedException {
        ObjectUtils.requireNotNull(property, PROPERTY);

        Object old = accountPropertiesRW.getResource(property);
        return !PropertyUtils.propertyObjectEquals(old, object);
    }

    /**
     * @param property    to be set and notified
     * @param object      data to be set
     * @param runOnThread thread to fire the listeners on
     * @return true if property state changed and listeners were notified
     */
    public boolean setAndNotify(AccountProperty property, Object object, OnThread runOnThread) throws AccountDeletedException {
        boolean propertyChanged = propertyDiffers(property, object);
        if (propertyChanged) {
            accountPropertiesRW.setResource(property, object);
            if (!propertyDiffers(property, object)) { // setter worked
                notifyListeners(property, accountPropertiesRW.getResource(property), runOnThread); // get set value
                notifyDependentProperties(property, runOnThread);
            } else {
                throw new IllegalStateException("Setter of account property " + property.name() + " doesn't set anything!");
            }
        }
        return propertyChanged;
    }

    private void notifyDependentProperties(AccountProperty property, OnThread runOnThread) throws AccountDeletedException {
        for (AccountProperty prop : property.getDependentProperties()) {
            if (property != prop) {
                notifyListeners(prop, accountPropertiesRW.getResource(prop), runOnThread);
            }
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

    private void notifyBackgroundListeners(Set<WrappedPropertyChangedListener> backgroundListeners, Object o) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0L, "") {
            @Override
            public void execute() {
                try {
                    notifyListeners(backgroundListeners, o);
                } catch (final Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        });
    }

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
