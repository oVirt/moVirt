package org.ovirt.mobile.movirt.util.properties;

import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.auth.CaCert;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.rest.dto.Api;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;
import org.ovirt.mobile.movirt.util.Version;

import java.util.Arrays;
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

    public enum OnThread {
        UI, BACKGROUND, CURRENT
    }

    // beware of circular injection, this is not guaranteed to be set when this class has been already injected somewhere else
    private MovirtAuthenticator authenticator;

    private boolean initialized = false;

    private static Map<AccountProperty, Set<WrappedPropertyChangedListener>> listeners;
    private static Map<AccountProperty, Set<WrappedPropertyChangedListener>> initQueueListeners;

    static {
        listeners = new EnumMap<>(AccountProperty.class);
        initQueueListeners = new EnumMap<>(AccountProperty.class);

        for (AccountProperty property : AccountProperty.values()) {
            listeners.put(property, new HashSet<WrappedPropertyChangedListener>());
            initQueueListeners.put(property, new HashSet<WrappedPropertyChangedListener>());
        }
    }

    @Bean
    void setAuthenticator(MovirtAuthenticator authenticator) {
        this.authenticator = authenticator;
        initialized = true;

        for (AccountProperty property : AccountProperty.values()) { // should be run in current thread
            Set<WrappedPropertyChangedListener> toNotify = initQueueListeners.get(property);
            if (!toNotify.isEmpty()) {
                notifyListeners(toNotify, authenticator.getResource(property));
            }
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
     * {@link org.ovirt.mobile.movirt.util.properties.AccountPropertiesManager#notifyListener(AccountProperty, PropertyChangedListener)} and
     * {@link org.ovirt.mobile.movirt.util.properties.AccountPropertiesManager#registerListener(AccountProperty, PropertyChangedListener)}
     */
    public <E> void notifyAndRegisterListener(final AccountProperty property, final PropertyChangedListener<E> listener) {
        notifyListener(property, listener);
        registerListener(property, listener);
    }

    /**
     * Runs on current thread
     *
     * @param property describes type of {@linkplain AccountProperty Account Property}.
     * @param listener is notified with present state of {@code property}.
     * @param <E>      type should correspond to {@code property} as described in documentation of {@linkplain AccountProperty Account Property}.
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@code property}.
     */
    @SuppressWarnings("unchecked")
    public <E> void notifyListener(final AccountProperty property, final PropertyChangedListener<E> listener) {
        if (isInitialized()) {
            listener.onPropertyChange((E) authenticator.getResource(property));
        } else {
            registerListenerImpl(initQueueListeners, property, listener);
        }
    }

    /**
     * Listener will be called from a thread decided by a caller of a setter of the property
     *
     * @param property describes type of {@linkplain AccountProperty Account Property}
     * @param listener listens for changes of {@code property}
     * @param <E>      type should correspond to {@code property} as described in documentation of {@linkplain AccountProperty Account Property}.
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@code property}
     * @see OnThread
     */
    @SuppressWarnings("unchecked")
    public <E> void registerListener(final AccountProperty property, final PropertyChangedListener<E> listener) {
        registerListenerImpl(listeners, property, listener);
    }

    private <E> void registerListenerImpl(Map<AccountProperty, Set<WrappedPropertyChangedListener>> listeners,
                                          AccountProperty property, final PropertyChangedListener<E> listener) {
        listeners.get(property).add(new WrappedPropertyChangedListener() {
            @Override
            public void onPropertyChange(Object newProperty) {
                listener.onPropertyChange((E) newProperty);
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
        return setAndNotify(runOnThread, AccountProperty.AUTH_TOKEN, token);
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
        return setAndNotify(runOnThread, AccountProperty.USERNAME, username);
    }

    public String getPassword() {
        return authenticator.getResource(AccountProperty.PASSWORD, String.class);
    }

    public boolean setPassword(String password) {  // triggers sync in later APIs (Android 6)
        return setPassword(password, OnThread.CURRENT);
    }

    public boolean setPassword(String password, OnThread runOnThread) {  // triggers sync in later APIs (Android 6)
        return setAndNotify(runOnThread, AccountProperty.PASSWORD, password);
    }

    public String getApiUrl() {
        return authenticator.getResource(AccountProperty.API_URL, String.class);
    }

    public boolean setApiUrl(String apiUrl) {
        return setApiUrl(apiUrl, OnThread.CURRENT);
    }

    public boolean setApiUrl(String apiUrl, OnThread runOnThread) {
        return setAndNotify(runOnThread, AccountProperty.API_URL, apiUrl);
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
        return setAndNotify(runOnThread, AccountProperty.VERSION, newVersion);
    }

    @NonNull
    public CertHandlingStrategy getCertHandlingStrategy() {
        return authenticator.getResource(AccountProperty.CERT_HANDLING_STRATEGY, CertHandlingStrategy.class);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy) {
        return setCertHandlingStrategy(certHandlingStrategy, OnThread.CURRENT);
    }

    public boolean setCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy, OnThread runOnThread) {
        return setAndNotify(runOnThread, AccountProperty.CERT_HANDLING_STRATEGY, certHandlingStrategy);
    }

    public Boolean hasAdminPermissions() {
        return authenticator.getResource(AccountProperty.HAS_ADMIN_PERMISSIONS, Boolean.class);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions) {
        return setAdminPermissions(hasAdminPermissions, OnThread.CURRENT);
    }

    public boolean setAdminPermissions(Boolean hasAdminPermissions, OnThread runOnThread) {
        return setAndNotify(runOnThread, AccountProperty.HAS_ADMIN_PERMISSIONS, hasAdminPermissions);
    }

    @NonNull
    public CaCert[] getCertificateChain() {
        return authenticator.getResource(AccountProperty.CERTIFICATE_CHAIN, CaCert[].class);
    }

    public boolean setCertificateChain(CaCert[] certChain) {
        return setCertificateChain(certChain, OnThread.CURRENT);
    }

    public boolean setCertificateChain(CaCert[] certChain, OnThread runOnThread) {
        return setAndNotify(runOnThread, AccountProperty.CERTIFICATE_CHAIN, certChain);
    }

    /**
     * @param property property to be checked against
     * @param object   data to be checked against
     * @return true if property state is different than object
     */
    public boolean propertyDiffers(AccountProperty property, Object object) {
        Object old = authenticator.getResource(property);
        boolean result;

        if (old == null || object == null) {
            result = old == object;
        } else if (old instanceof Object[] && object instanceof Object[]) {
            result = Arrays.equals((Object[]) old, (Object[]) object);
        } else {
            result = old.equals(object);
        }

        return !result;
    }

    /**
     * @param property to be set and notified
     * @param object   data to be set
     * @return true if property state changed and listeners were notified
     */
    private boolean setAndNotify(OnThread runOnThread, AccountProperty property, Object object) {
        boolean propertyChanged = propertyDiffers(property, object);
        if (propertyChanged) {
            authenticator.setResource(property, object);
            if (!propertyDiffers(property, object)) { // setter worked
                notifyListeners(runOnThread, property, authenticator.getResource(property)); // get set value
                switch (property) {
                    case API_URL:
                        notifyListeners(runOnThread, AccountProperty.API_BASE_URL, authenticator.getResource(AccountProperty.API_BASE_URL));
                        break;
                }
            } else {
                throw new IllegalStateException("Setter of " + property.name() + " doesn't set anything!");
            }
        }
        return propertyChanged;
    }

    private void notifyListeners(OnThread runOnThread, AccountProperty property, Object o) {
        Set<WrappedPropertyChangedListener> propertyListeners = listeners.get(property);
        switch (runOnThread) {
            case UI:
                notifyUiListeners(propertyListeners, o);
                break;
            case BACKGROUND:
                notifyBackgroundListeners(propertyListeners, o);
                break;
            case CURRENT:
                notifyListeners(propertyListeners, o);
                break;
        }
    }

    private void notifyListeners(Set<WrappedPropertyChangedListener> currentListeners, Object o) {
        for (WrappedPropertyChangedListener listener : currentListeners) {
            listener.onPropertyChange(o);
        }
    }

    /**
     * This method should not be used outside of {@link AccountPropertiesManager}
     */
    @Background
    void notifyBackgroundListeners(Set<WrappedPropertyChangedListener> backgroundListeners, Object o) {
        notifyListeners(backgroundListeners, o);
    }

    /**
     * This method should not be used outside of {@link AccountPropertiesManager}
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void notifyUiListeners(Set<WrappedPropertyChangedListener> uiThreadListeners, Object o) {
        notifyListeners(uiThreadListeners, o);
    }

    interface WrappedPropertyChangedListener {
        void onPropertyChange(Object o);
    }
}
