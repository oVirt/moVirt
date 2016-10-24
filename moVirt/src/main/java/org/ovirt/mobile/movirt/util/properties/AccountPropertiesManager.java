package org.ovirt.mobile.movirt.util.properties;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.rest.dto.Api;
import org.ovirt.mobile.movirt.util.Version;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EBean(scope = EBean.Scope.Singleton)
public class AccountPropertiesManager {
    private static final String TAG = AccountPropertiesManager.class.getSimpleName();

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
     * @param property describes type of {@linkplain AccountProperty Account Property}
     * @param listener listens for changes of {@code property}
     * @param <E>      type should correspond to {@code property} as described in documentation of {@linkplain AccountProperty Account Property}.
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@code property}
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

    /**
     * @param property describes type of {@linkplain AccountProperty Account Property}.
     * @param listener is notified with present state of {@code property}
     * @param <E>      type should correspond to {@code property} as described in documentation of {@linkplain AccountProperty Account Property}.
     * @throws ClassCastException if {@code <E>} doesn't correspond to {@code property}
     */
    @SuppressWarnings("unchecked")
    public <E> void notifyListener(final AccountProperty property, final PropertyChangedListener<E> listener) {
        switch (property) {
            case VERSION:
                listener.onPropertyChange((E) getApiVersion());
                break;
        }
    }

    public void setApiVersion(Api newApi) {
        Version newVersion = newApi == null ? null : newApi.toVersion();

        if (!getApiVersion().equals(newVersion)) {
            authenticator.setApiVersion(newVersion);

            // we get fresh version in case newApi is null
            Version version = getApiVersion();
            notifyListeners(AccountProperty.VERSION, version);
        }
    }

    public Version getApiVersion() {
        return authenticator.getApiVersion();
    }

    private void notifyListeners(AccountProperty property, Object o) {
        for (WrappedPropertyChangedListener listener : listeners.get(property)) {
            listener.onPropertyChange(o);
        }
    }

    private interface WrappedPropertyChangedListener {
        void onPropertyChange(Object o);
    }
}
