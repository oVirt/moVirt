package org.ovirt.mobile.movirt.util.properties;

import android.accounts.AccountManagerFuture;
import android.os.Bundle;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.rest.dto.Api;
import org.ovirt.mobile.movirt.ui.CertHandlingStrategy;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.Version;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EBean(scope = EBean.Scope.Singleton)
public class AccountPropertiesManager {
    private static final String TAG = AccountPropertiesManager.class.getSimpleName();

    public enum OnThread {
        UI, BACKGROUND, CURRENT
    }

    @Bean
    MovirtAuthenticator authenticator;

    private Map<AccountProperty, Set<WrappedPropertyChangedListener>> listeners;

    @AfterInject
    public void init() {
        listeners = new HashMap<>(AccountProperty.values().length);
        for (AccountProperty property : AccountProperty.values()) {
            listeners.put(property, new HashSet<WrappedPropertyChangedListener>());
        }
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
        listener.onPropertyChange((E) authenticator.getResource(property));
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

    /**
     * @param property property to be checked against
     * @param object   data to be checked against
     * @return true if property state is different than object
     */
    public boolean propertyDiffers(AccountProperty property, Object object) {
        return !ObjectUtils.equals(authenticator.getResource(property), object);
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
        Set<WrappedPropertyChangedListener> listeners = this.listeners.get(property);
        switch (runOnThread) {
            case UI:
                notifyUiListeners(listeners, o);
                break;
            case BACKGROUND:
                notifyBackgroundListeners(listeners, o);
                break;
            case CURRENT:
                notifyListeners(listeners, o);
                break;
        }
    }

    private void notifyListeners(Set<WrappedPropertyChangedListener> backgroundListeners, Object o) {
        for (WrappedPropertyChangedListener listener : backgroundListeners) {
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
