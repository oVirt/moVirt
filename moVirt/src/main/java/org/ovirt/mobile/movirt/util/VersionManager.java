package org.ovirt.mobile.movirt.util;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;
import org.ovirt.mobile.movirt.rest.dto.Api;

import java.util.ArrayList;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class VersionManager {
    private static final String TAG = VersionManager.class.getSimpleName();

    @Bean
    MovirtAuthenticator authenticator;

    private List<ApiVersionChangedListener> listeners = new ArrayList<>();
    private Version version;

    @AfterInject
    public void init() {
        version = authenticator.getApiVersion();
    }

    public void registerListener(ApiVersionChangedListener listener) {
        listeners.add(listener);
    }

    public void setApiVersion(Api newApi) {
        Version newVersion = newApi == null ? null : newApi.toVersion();

        if (!authenticator.getApiVersion().equals(newVersion)) {
            authenticator.setApiVersion(newVersion);

            // we get default, if newVersion is null
            version = authenticator.getApiVersion();

            // notify
            for (ApiVersionChangedListener listener : listeners) {
                listener.onVersionChanged(version);
            }
        }
    }

    public Version getApiVersion() {
        return version;
    }

    /**
     * Call listener with current version
     */
    public void notifyListener(ApiVersionChangedListener listener) {
        listener.onVersionChanged(version);
    }

    public interface ApiVersionChangedListener {
        void onVersionChanged(Version version);
    }
}
