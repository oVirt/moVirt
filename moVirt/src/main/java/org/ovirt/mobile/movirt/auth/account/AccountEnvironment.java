package org.ovirt.mobile.movirt.auth.account;

import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;

import org.ovirt.mobile.movirt.Constants;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.auth.properties.AccountPropertiesRW;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.facade.BaseEntityFacade;
import org.ovirt.mobile.movirt.facade.ClusterFacade_;
import org.ovirt.mobile.movirt.facade.ConsoleFacade_;
import org.ovirt.mobile.movirt.facade.DatacenterFacade_;
import org.ovirt.mobile.movirt.facade.DiskAttachmentsFacade_;
import org.ovirt.mobile.movirt.facade.DiskFacade_;
import org.ovirt.mobile.movirt.facade.EntityFacade;
import org.ovirt.mobile.movirt.facade.EventFacade;
import org.ovirt.mobile.movirt.facade.EventFacade_;
import org.ovirt.mobile.movirt.facade.HostEventFacade;
import org.ovirt.mobile.movirt.facade.HostEventFacade_;
import org.ovirt.mobile.movirt.facade.HostFacade_;
import org.ovirt.mobile.movirt.facade.NicFacade_;
import org.ovirt.mobile.movirt.facade.SnapshotDiskFacade_;
import org.ovirt.mobile.movirt.facade.SnapshotFacade_;
import org.ovirt.mobile.movirt.facade.SnapshotNicFacade_;
import org.ovirt.mobile.movirt.facade.StorageDomainEventFacade;
import org.ovirt.mobile.movirt.facade.StorageDomainEventFacade_;
import org.ovirt.mobile.movirt.facade.StorageDomainFacade_;
import org.ovirt.mobile.movirt.facade.VmEventFacade;
import org.ovirt.mobile.movirt.facade.VmEventFacade_;
import org.ovirt.mobile.movirt.facade.VmFacade_;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Console;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.DiskAttachment;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.SnapshotDisk;
import org.ovirt.mobile.movirt.model.SnapshotNic;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.base.BaseEntity;
import org.ovirt.mobile.movirt.model.base.OVirtAccountEntity;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.EventProviderHelper;
import org.ovirt.mobile.movirt.provider.EventProviderHelper_;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.provider.UriMatcher;
import org.ovirt.mobile.movirt.provider.ViewHelper;
import org.ovirt.mobile.movirt.provider.Views;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.RequestHandler_;
import org.ovirt.mobile.movirt.rest.RestErrorHandler;
import org.ovirt.mobile.movirt.rest.RestErrorHandler_;
import org.ovirt.mobile.movirt.rest.client.LoginClient;
import org.ovirt.mobile.movirt.rest.client.LoginClient_;
import org.ovirt.mobile.movirt.rest.client.OVirtClient;
import org.ovirt.mobile.movirt.rest.client.OVirtClient_;
import org.ovirt.mobile.movirt.rest.client.VvClient;
import org.ovirt.mobile.movirt.rest.client.VvClient_;
import org.ovirt.mobile.movirt.rest.client.requestfactory.OvirtSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.client.requestfactory.OvirtSimpleClientHttpRequestFactory_;
import org.ovirt.mobile.movirt.util.DestroyableListeners;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.message.MessageHelper_;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper_;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccountEnvironment {

    private final SharedPreferencesHelper sharedPreferencesHelper;
    private final AccountPropertiesManager propertiesManager;
    private final MessageHelper messageHelper;

    private final OvirtSimpleClientHttpRequestFactory timeoutRequestFactory; // do not move local - will be disposed by reflection
    private final OvirtSimpleClientHttpRequestFactory requestFactory;
    private final LoginClient loginClient;
    private final OVirtClient oVirtClient;
    private final VvClient vvClient;

    private final RestErrorHandler restErrorHandler;
    private final RequestHandler requestHandler;

    private final EventProviderHelper eventProviderHelper;

    private final Map<Class<?>, EntityFacade<?>> facades = new HashMap<>();

    private final StorageDomainEventFacade storageDomainEventFacade;
    private final HostEventFacade hostEventFacade;
    private final VmEventFacade vmEventFacade;

    private volatile boolean loginInProgress;
    private volatile boolean certificateDownloadInProgress;
    private volatile boolean inSync;

    private volatile Version version;

    private DestroyableListeners listeners;

    private ProviderFacade providerFacade;

    public AccountEnvironment(Context context, AccountManager accountManager, ProviderFacade providerFacade, MovirtAccount account) {
        this.providerFacade = providerFacade;
        sharedPreferencesHelper = SharedPreferencesHelper_.getInstance_(context)
                .initialize(account);

        propertiesManager = new AccountPropertiesManager(new AccountPropertiesRW(accountManager, account));

        messageHelper = MessageHelper_.getInstance_(context)
                .setSharedPreferencesHelper(sharedPreferencesHelper)
                .setPropertiesManager(propertiesManager);

        timeoutRequestFactory = OvirtSimpleClientHttpRequestFactory_.getInstance_(context)
                .setTimeout(Constants.MAX_LOGIN_TIMEOUT)
                .init(propertiesManager, messageHelper);

        loginClient = LoginClient_.getInstance_(context)
                .init(propertiesManager, timeoutRequestFactory);

        requestFactory = OvirtSimpleClientHttpRequestFactory_.getInstance_(context)
                .init(propertiesManager, messageHelper);

        restErrorHandler = RestErrorHandler_.getInstance_(context)
                .init(account, messageHelper, sharedPreferencesHelper);

        requestHandler = RequestHandler_.getInstance_(context)
                .withDefaultErrorHandler(restErrorHandler)
                .init(propertiesManager);

        oVirtClient = OVirtClient_.getInstance_(context)
                .init(propertiesManager, messageHelper, requestFactory, requestHandler, sharedPreferencesHelper);

        vvClient = VvClient_.getInstance_(context)
                .init(propertiesManager, requestFactory, requestHandler);

        eventProviderHelper = EventProviderHelper_.getInstance_(context)
                .init(account, sharedPreferencesHelper, messageHelper);

        listeners = new DestroyableListeners(propertiesManager)
                .notifyAndRegisterListener(new AccountProperty.VersionListener() {
                    @Override
                    public void onPropertyChange(Version newVersion) {
                        version = newVersion;
                    }
                });

        addFacade(DataCenter.class, DatacenterFacade_.getInstance_(context));
        addFacade(Cluster.class, ClusterFacade_.getInstance_(context));
        addFacade(Vm.class, VmFacade_.getInstance_(context));
        addFacade(Host.class, HostFacade_.getInstance_(context));
        addFacade(StorageDomain.class, StorageDomainFacade_.getInstance_(context));
        addFacade(Disk.class, DiskFacade_.getInstance_(context));
        addFacade(DiskAttachment.class, DiskAttachmentsFacade_.getInstance_(context));
        addFacade(Nic.class, NicFacade_.getInstance_(context));
        addFacade(Snapshot.class, SnapshotFacade_.getInstance_(context));
        addFacade(SnapshotDisk.class, SnapshotDiskFacade_.getInstance_(context));
        addFacade(SnapshotNic.class, SnapshotNicFacade_.getInstance_(context));
        addFacade(Console.class, ConsoleFacade_.getInstance_(context));

        final EventFacade eventFacade = EventFacade_.getInstance_(context);
        eventFacade.setEventProviderHelper(eventProviderHelper);
        addFacade(Event.class, eventFacade);

        storageDomainEventFacade = StorageDomainEventFacade_.getInstance_(context);
        storageDomainEventFacade.init(propertiesManager, oVirtClient, requestHandler);

        hostEventFacade = HostEventFacade_.getInstance_(context);
        hostEventFacade.init(propertiesManager, oVirtClient, requestHandler);

        vmEventFacade = VmEventFacade_.getInstance_(context);
        vmEventFacade.init(propertiesManager, oVirtClient, requestHandler);
    }

    @SuppressWarnings("unchecked")
    public <E extends BaseEntity<?>> void destroy() {
        String accountId = propertiesManager.getManagedAccount().getId();

        listeners.destroy();

        // dispose all EnvDisposable classes
        for (Field field : AccountEnvironment.class.getDeclaredFields()) {
            if (EnvDisposable.class.isAssignableFrom(field.getType())) {
                try {
                    ((EnvDisposable) field.get(this)).dispose();
                } catch (IllegalAccessException ignore) {
                }
            }
        }

        Set<Class<?>> tables = new HashSet<>(UriMatcher.getInstance().getClasses());
        for (ViewHelper.View view : Views.getViews()) { // do not clear views
            tables.remove(view.clazz);
        }

        for (Class<?> clazz : tables) { // delete all entities
            if (OVirtAccountEntity.class.isAssignableFrom(clazz)) {
                providerFacade.query((Class<E>) clazz)
                        .where(OVirtAccountEntity.ACCOUNT_ID, accountId)
                        .delete();
            }
        }
    }

    @NonNull
    public SharedPreferencesHelper getSharedPreferencesHelper() {
        return sharedPreferencesHelper;
    }

    @NonNull
    public AccountPropertiesManager getAccountPropertiesManager() {
        return propertiesManager;
    }

    @NonNull
    public MessageHelper getMessageHelper() {
        return messageHelper;
    }

    @NonNull
    public LoginClient getLoginClient() {
        return loginClient;
    }

    @NonNull
    public OVirtClient getOVirtClient() {
        return oVirtClient;
    }

    public VvClient getVvClient() {
        return vvClient;
    }

    @NonNull
    public RestErrorHandler getRestErrorHandler() {
        return restErrorHandler;
    }

    @NonNull
    public EventProviderHelper getEventProviderHelper() {
        return eventProviderHelper;
    }

    @NonNull
    public Version getVersion() {
        return version;
    }

    public boolean isLoginInProgress() {
        return loginInProgress;
    }

    void setLoginInProgress(boolean loginInProgress) {
        this.loginInProgress = loginInProgress;
    }

    public boolean isCertificateDownloadInProgress() {
        return certificateDownloadInProgress;
    }

    void setCertificateDownloadInProgress(boolean certificateDownloadInProgress) {
        this.certificateDownloadInProgress = certificateDownloadInProgress;
    }

    public boolean isInSync() {
        return inSync;
    }

    void setInSync(boolean inSync) {
        this.inSync = inSync;
    }

    @SuppressWarnings("unchecked")
    public <E extends OVirtEntity, T extends EntityFacade<E>> T getFacade(Class<?> clazz) {
        return (T) facades.get(clazz);
    }

    public StorageDomainEventFacade getStorageDomainEventFacade() {
        return storageDomainEventFacade;
    }

    public HostEventFacade getHostEventFacade() {
        return hostEventFacade;
    }

    public VmEventFacade getVmEventFacade() {
        return vmEventFacade;
    }

    private <E extends OVirtEntity> void addFacade(Class<E> clazz, BaseEntityFacade<E> facade) {
        facade.init(propertiesManager, oVirtClient, requestHandler);
        facades.put(clazz, facade);
    }

    public interface EnvDisposable {
        void dispose();
    }
}
