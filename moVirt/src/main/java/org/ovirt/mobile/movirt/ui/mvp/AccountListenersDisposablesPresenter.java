package org.ovirt.mobile.movirt.ui.mvp;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.auth.account.EnvironmentStore;
import org.ovirt.mobile.movirt.auth.properties.PropertyChangedListener;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EBean
public abstract class AccountListenersDisposablesPresenter<P extends AccountListenersDisposablesPresenter, V extends FinishableView>
        extends AccountDisposablesPresenter<P, V> {

    private List<PropertyChangedListener> listeners = new ArrayList<>();

    @Bean
    public EnvironmentStore envStore;

    protected Collection<PropertyChangedListener> getPropertyChangedListeners() {
        return listeners;
    }

    @Override
    public void destroy() {
        super.destroy();

        try {
            final AccountPropertiesManager accountPropertiesManager = envStore.getAccountPropertiesManager(getAccount());
            for (PropertyChangedListener listener : listeners) {
                if (listener != null) {
                    accountPropertiesManager.removeListener(listener);
                }
            }
        } catch (AccountDeletedException ignore) {
        }

        listeners.clear();
    }
}
