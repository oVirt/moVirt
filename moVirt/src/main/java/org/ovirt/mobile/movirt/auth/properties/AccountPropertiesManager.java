package org.ovirt.mobile.movirt.auth.properties;

import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.Version;
import org.ovirt.mobile.movirt.rest.dto.Api;
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
@EBean(scope = EBean.Scope.Singleton)
public class AccountPropertiesManager {
    private static final String TAG = AccountPropertiesManager.class.getSimpleName();

    private static final String PROPERTY = "property";
    private static final String LISTENER = "listener";

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

    // beware of circular injection, this is not guaranteed to be set when this class has been already injected somewhere else
    private MovirtAuthenticator authenticator;

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

    @Bean
    void setAuthenticator(MovirtAuthenticator authenticator) {
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
     * {@link org.ovirt.mobile.movirt.auth.properties.AccountPropertiesManager#notifyListener(AccountProperty, PropertyChangedListener)} and
     * {@link org.ovirt.mobile.movirt.auth.properties.AccountPropertiesManager#registerListener(AccountProperty, PropertyChangedListener)}
     */
    public <E> void notifyAndRegisterListener(final AccountProperty property, final PropertyChangedListener<E> listener) {
        notifyListener(property, listener);
        registerListener(property, listener);
    }

    /**
     * Notifies listener
     * The listener IS GUARANTEED to be called from current thread, UNLESS caller of this method created new thread in @AfterViews.
     *
     * @param property describes type of {@linkplain AccountProperty Account Property}.
     * @param listener is notified with present state of {@code property}.
     * @param <E>      type should correspond to {@code property} as described in documentation of {@linkplain AccountProperty Account Property}.
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@code property}.
     */
    @SuppressWarnings("unchecked")
    public <E> void notifyListener(final AccountProperty property, final PropertyChangedListener<E> listener) {
        if (isInitialized()) {
            ObjectUtils.requireNotNull(property, PROPERTY);
            ObjectUtils.requireNotNull(listener, LISTENER);
            listener.onPropertyChange((E) authenticator.getResource(property));
        } else {
            registerListenerImpl(initQueueListeners, property, listener);
        }
    }

    /**
     * Registers listener.
     * The listener IS NOT GUARANTEED to be called from the main UI thread (listener will be called from
     * a thread specified by a caller of a setter of the property)
     *
     * @param property describes type of {@linkplain AccountProperty Account Property}
     * @param listener listens for changes of {@code property}
     * @param <E>      type should correspond to {@code property} as described in documentation of {@linkplain AccountProperty Account Property}.
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@code property}
     * @see OnThread
     */
    public <E> void registerListener(final AccountProperty property, final PropertyChangedListener<E> listener) {

        registerListenerImpl(listeners, property, listener);
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
                                          AccountProperty property, final PropertyChangedListener<E> listener) {
        ObjectUtils.requireNotNull(property, PROPERTY);
        ObjectUtils.requireNotNull(listener, LISTENER);

        listeners.get(property).add(new WrappedPropertyChangedListener() {
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

    @SuppressWarnings("unchecked")
    public AccountManagerFuture<Bundle> getAuthToken() {
        return authenticator.getResource(AccountProperty.FUTURE_AUTH_TOKEN, AccountManagerFuture.class);
    }

    public String peekAuthToken() {
        return authenticator.getResource(AccountProperty.PEEK_AUTH_TOKEN, String.class);
    }

    public boolean setAuthToken(String token) {
        return setAuthToken(token, OnThread.CURRENT);
    }

    public boolean setAuthToken(String token, OnThread runOnThread) {
        return setAndNotify(AccountProperty.AUTH_TOKEN, token, runOnThread);
    }

    public Boolean accountConfigured() {
        return authenticator.getResource(AccountProperty.ACCOUNT_CONFIGURED, Boolean.class);
    }

    public String getUsername() {
        return authenticator.getResource(AccountProperty.USERNAME, String.class);
    }

    public boolean setUsername(String username) {
        return setUsername(username, OnThread.CURRENT);
    }

    public boolean setUsername(String username, OnThread runOnThread) {
        return setAndNotify(AccountProperty.USERNAME, username, runOnThread);
    }

    public String getPassword() {
        return authenticator.getResource(AccountProperty.PASSWORD, String.class);
    }

    public boolean setPassword(String password) {  // triggers sync in later APIs (Android 6)
        return setPassword(password, OnThread.CURRENT);
    }

    public boolean setPassword(String password, OnThread runOnThread) {  // triggers sync in later APIs (Android 6)
        return setAndNotify(AccountProperty.PASSWORD, password, runOnThread);
    }

    public String getApiUrl() {
        return authenticator.getResource(AccountProperty.API_URL, String.class);
    }

    public boolean setApiUrl(String apiUrl) {
        return setApiUrl(apiUrl, OnThread.CURRENT);
    }

    public boolean setApiUrl(String apiUrl, OnThread runOnThread) {
        return setAndNotify(AccountProperty.API_URL, apiUrl, runOnThread);
    }

    public String getApiBaseUrl() {
        return authenticator.getResource(AccountProperty.API_BASE_URL, String.class);
    }

    @NonNull
    public Version getApiVersion() {
        return authenticator.getResource(AccountProperty.VERSION, Version.class);
    }

    public boolean setApiVersion(Api newApi) {
        return setApiVersion(newApi, OnThread.CURRENT);
    }

    public boolean setApiVersion(Api newApi, OnThread runOnThread) {
        Version newVersion = newApi == null ? null : newApi.toVersion();
        return setAndNotify(AccountProperty.VERSION, newVersion, runOnThread);
    }

    @NonNull
    public CertHandlingStrategy getCertHandlingStrategy() {
        return authenticator.getResource(AccountProperty.CERT_HANDLING_STRATEGY, CertHandlingStrategy.class);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy) {
        return setCertHandlingStrategy(certHandlingStrategy, OnThread.CURRENT);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy, OnThread runOnThread) {
        return setAndNotify(AccountProperty.CERT_HANDLING_STRATEGY, certHandlingStrategy, runOnThread);
    }

    public Boolean hasAdminPermissions() {
        return authenticator.getResource(AccountProperty.HAS_ADMIN_PERMISSIONS, Boolean.class);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions) {
        return setAdminPermissions(hasAdminPermissions, OnThread.CURRENT);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions, OnThread runOnThread) {
        return setAndNotify(AccountProperty.HAS_ADMIN_PERMISSIONS, hasAdminPermissions, runOnThread);
    }

    @NonNull
    public Cert[] getCertificateChain() {
        return authenticator.getResource(AccountProperty.CERTIFICATE_CHAIN, Cert[].class);
    }

    public boolean setCertificateChain(Cert[] certChain) {
        return setCertificateChain(certChain, OnThread.CURRENT);
    }

    public boolean setCertificateChain(Cert[] certChain, OnThread runOnThread) {
        return setAndNotify(AccountProperty.CERTIFICATE_CHAIN, certChain, runOnThread);
    }

    @NonNull
    public String getValidHostnames() {
        return authenticator.getResource(AccountProperty.VALID_HOSTNAMES, String.class);
    }

    @NonNull
    public String[] getValidHostnameList() {
        return authenticator.getResource(AccountProperty.VALID_HOSTNAME_LIST, String[].class);
    }

    public boolean setValidHostnameList(String[] hostnameList) {
        return setValidHostnameList(hostnameList, OnThread.CURRENT);
    }

    public boolean setValidHostnameList(String[] hostnameList, OnThread runOnThread) {
        return setAndNotify(AccountProperty.VALID_HOSTNAME_LIST, hostnameList, runOnThread);
    }

    @NonNull
    public Boolean isCustomCertificateLocation() {
        return authenticator.getResource(AccountProperty.CUSTOM_CERTIFICATE_LOCATION, Boolean.class);
    }

    public boolean setCustomCertificateLocation(Boolean customCertificateLocation) {
        return setCustomCertificateLocation(customCertificateLocation, OnThread.CURRENT);
    }

    public boolean setCustomCertificateLocation(Boolean customCertificateLocation, OnThread runOnThread) {
        return setAndNotify(AccountProperty.CUSTOM_CERTIFICATE_LOCATION, customCertificateLocation, runOnThread);
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
    private boolean setAndNotify(AccountProperty property, Object object, OnThread runOnThread) {
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

    private void notifyListeners(Set<WrappedPropertyChangedListener> currentListeners, Object o) {
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
    @Background
    void notifyBackgroundListeners(Set<WrappedPropertyChangedListener> backgroundListeners, Object o) {
        notifyListeners(backgroundListeners, o);
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
