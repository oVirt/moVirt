package org.ovirt.mobile.movirt.rest.client;

import android.content.Context;
import android.support.annotation.NonNull;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.ovirt.mobile.movirt.MoVirtApp;
import org.ovirt.mobile.movirt.auth.account.AccountEnvironment;
import org.ovirt.mobile.movirt.auth.properties.AccountProperty;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.Version;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
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
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.RequestHandler;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;
import org.ovirt.mobile.movirt.rest.client.requestfactory.OvirtSimpleClientHttpRequestFactory;
import org.ovirt.mobile.movirt.rest.dto.Action;
import org.ovirt.mobile.movirt.rest.dto.Events;
import org.ovirt.mobile.movirt.rest.dto.SnapshotAction;
import org.ovirt.mobile.movirt.util.DestroyableListeners;
import org.ovirt.mobile.movirt.util.IdHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.message.MessageHelper;
import org.ovirt.mobile.movirt.util.preferences.SettingsKey;
import org.ovirt.mobile.movirt.util.preferences.SharedPreferencesHelper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.ovirt.mobile.movirt.rest.RestHelper.setAcceptEncodingHeaderAndFactory;
import static org.ovirt.mobile.movirt.rest.RestHelper.setFilterHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setVersionHeader;
import static org.ovirt.mobile.movirt.rest.RestHelper.setupAuth;

@EBean
public class OVirtClient implements AccountEnvironment.EnvDisposable {

    private AccountPropertiesManager propertiesManager;

    private SharedPreferencesHelper sharedPreferencesHelper;

    private MessageHelper messageHelper;

    private RequestHandler requestHandler;

    private Version version;

    private String accountId;

    private DestroyableListeners listeners;

    @App
    MoVirtApp app;

    @RootContext
    Context context;

    @RestService
    OVirtRestClient restClient;

    @Bean
    ProviderFacade providerFacade;

    public OVirtClient init(AccountPropertiesManager propertiesManager, MessageHelper messageHelper, OvirtSimpleClientHttpRequestFactory requestFactory,
                            RequestHandler requestHandler, SharedPreferencesHelper sharedPreferencesHelper) {
        ObjectUtils.requireAllNotNull(propertiesManager, messageHelper, requestFactory, requestHandler, sharedPreferencesHelper);

        this.propertiesManager = propertiesManager;
        this.accountId = propertiesManager.getManagedAccount().getId();
        this.messageHelper = messageHelper;
        this.requestHandler = requestHandler;
        this.sharedPreferencesHelper = sharedPreferencesHelper;

        setAcceptEncodingHeaderAndFactory(restClient, requestFactory);

        listeners = new DestroyableListeners(propertiesManager)
                .notifyAndRegisterListener(new AccountProperty.VersionListener() {
                    @Override
                    public void onPropertyChange(Version newVersion) {
                        setVersionHeader(restClient, newVersion);
                        setupAuth(restClient, newVersion);
                        version = newVersion;
                    }
                }).notifyAndRegisterListener(new AccountProperty.ApiUrlListener() {
                    @Override
                    public void onPropertyChange(String apiUrl) {
                        restClient.setRootUrl(apiUrl);
                    }
                }).notifyAndRegisterListener(new AccountProperty.HasAdminPermissionsListener() {
                    @Override
                    public void onPropertyChange(Boolean hasAdminPermissions) {
                        setFilterHeader(restClient, hasAdminPermissions);
                    }
                });

        return this;
    }

    @Override
    public void dispose() {
        listeners.destroy();
    }

    public void startVm(final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.startVm(new Action(), IdHelper.getIdPart(vmId));
                return null;
            }
        }, response);
    }

    public void stopVm(final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.stopVm(new Action(), IdHelper.getIdPart(vmId));
                return null;
            }
        }, response);
    }

    public void rebootVm(final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.rebootVm(new Action(), IdHelper.getIdPart(vmId));
                return null;
            }
        }, response);
    }

    public void migrateVmToHost(final String vmId, final String hostId, Response<Void> response) {
        final String realVmId = IdHelper.getIdPart(vmId);
        final String realHostId = IdHelper.getIdPart(hostId);

        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                Action action = version.isV3Api() ? new org.ovirt.mobile.movirt.rest.dto.v3.ActionMigrate(realHostId) :
                        new org.ovirt.mobile.movirt.rest.dto.v4.ActionMigrate(realHostId);
                restClient.migrateVmToHost(action, realVmId);
                return null;
            }
        }, response);
    }

    public void migrateVmToDefaultHost(final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.migrateVmToHost(new Action(), IdHelper.getIdPart(vmId));
                return null;
            }
        }, response);
    }

    public void cancelMigration(final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.cancelMigration(new Action(), IdHelper.getIdPart(vmId));
                return null;
            }
        }, response);
    }

    @NonNull
    public Request<Vm> getVmRequest(final String vmId) {
        final String realVmId = IdHelper.getIdPart(vmId);

        return new RestClientRequest<Vm>() {
            @Override
            public Vm fire() {
                org.ovirt.mobile.movirt.rest.dto.Vm vm = version.isV3Api() ? restClient.getVmV3(realVmId) : restClient.getVmV4(realVmId);
                return vm.toEntity(accountId);
            }
        };
    }

    public void activateHost(final String hostId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.activateHost(new Action(), IdHelper.getIdPart(hostId));
                return null;
            }
        }, response);
    }

    public void dectivateHost(final String hostId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.deactivateHost(new Action(), IdHelper.getIdPart(hostId));
                return null;
            }
        }, response);
    }

    public void deleteSnapshot(final String vmId, final String snapshotId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.deleteSnapshot(IdHelper.getIdPart(vmId), IdHelper.getIdPart(snapshotId));
                return null;
            }
        }, response);
    }

    public void restoreSnapshot(final SnapshotAction snapshotAction, final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                final String realVmId = IdHelper.getIdPart(vmId);
                String realSnapshotId = IdHelper.getIdPart(snapshotAction.snapshot.id);
                SnapshotAction restAction = new SnapshotAction(snapshotAction.restore_memory);

                restClient.restoreSnapshot(restAction, realVmId, realSnapshotId);
                return null;
            }
        }, response);
    }

    public void previewSnapshot(final SnapshotAction snapshotAction, final String vmId, Response<Void> response) {
        snapshotAction.snapshot.id = IdHelper.getIdPart(snapshotAction.snapshot.id);

        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                if (version.isV3Api()) {
                    restClient.previewSnapshotV3(snapshotAction, IdHelper.getIdPart(vmId));
                } else {
                    restClient.previewSnapshotV4(snapshotAction, IdHelper.getIdPart(vmId));
                }
                return null;
            }
        }, response);
    }

    public void createSnapshot(final org.ovirt.mobile.movirt.rest.dto.Snapshot snapshot, final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                restClient.createSnapshot(snapshot, IdHelper.getIdPart(vmId));
                return null;
            }
        }, response);
    }

    public void commitSnapshot(final String vmId, Response<Void> response) {
        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                if (version.isV3Api()) {
                    restClient.commitSnapshotV3(new Action(), IdHelper.getIdPart(vmId));
                } else {
                    restClient.commitSnapshotV4(new Action(), IdHelper.getIdPart(vmId));
                }
                return null;
            }
        }, response);
    }

    public void undoSnapshot(final String vmId, Response<Void> response) {
        final String realVmId = IdHelper.getIdPart(vmId);

        requestHandler.fireRestRequestSafe(new RestClientRequest<Void>() {
            @Override
            public Void fire() {
                if (version.isV3Api()) {
                    restClient.undoSnapshotV3(new Action(), realVmId);
                } else {
                    restClient.undoSnapshotV4(new Action(), realVmId);
                }
                return null;
            }
        }, response);
    }

    @NonNull
    public Request<Host> getHostRequest(final String hostId) {
        final String realHostId = IdHelper.getIdPart(hostId);

        return new RestClientRequest<Host>() {
            @Override
            public Host fire() {
                org.ovirt.mobile.movirt.rest.dto.Host wrapper = version.isV3Api() ?
                        restClient.getHostV3(realHostId) : restClient.getHostV4(realHostId);
                return wrapper.toEntity(accountId);
            }
        };
    }

    @NonNull
    public Request<StorageDomain> getStorageDomainRequest(final String storageDomainId) {
        final String realSdId = IdHelper.getIdPart(storageDomainId);

        return new RestClientRequest<StorageDomain>() {
            @Override
            public StorageDomain fire() {
                org.ovirt.mobile.movirt.rest.dto.StorageDomain wrapper = version.isV3Api() ?
                        restClient.getStorageDomainV3(realSdId) :
                        restClient.getStorageDomainV4(realSdId);
                return wrapper.toEntity(accountId);
            }
        };
    }

    public Request<List<DiskAttachment>> getDisksAttachmentsRequest(final String vmId) {
        return new RestClientRequest<List<DiskAttachment>>() {
            @Override
            public List<DiskAttachment> fire() {
                VersionSupport.DISK_ATTACHMENTS.throwIfNotSupported(version);

                return mapToEntities(restClient.getDisksAttachmentsV4(IdHelper.getIdPart(vmId)));
            }
        };
    }

    public Request<List<SnapshotDisk>> getSnapshotDisksRequest(final String vmId, final String snapshotId) {
        final String realVmId = IdHelper.getIdPart(vmId);
        final String realSnapshotId = IdHelper.getIdPart(snapshotId);

        return new RestClientRequest<List<SnapshotDisk>>() {
            @Override
            public List<SnapshotDisk> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.dto.SnapshotDisk> snapshotDisks;

                if (version.isV3Api()) {
                    snapshotDisks = restClient.getSnapshotDisksV3(realVmId, realSnapshotId);
                } else {
                    snapshotDisks = restClient.getSnapshotDisksV4(realVmId, realSnapshotId);
                }

                if (snapshotDisks != null) {
                    for (org.ovirt.mobile.movirt.rest.dto.SnapshotDisk disk : snapshotDisks.getList()) {
                        disk.vmId = realVmId;
                        disk.snapshotId = realSnapshotId;
                    }
                }

                return mapToEntities(snapshotDisks);
            }
        };
    }

    public Request<List<Disk>> getDisksRequest(final String vmId) {
        final boolean downloadAll = vmId == null;
        final String realVmId = IdHelper.getIdPart(vmId);

        return new RestClientRequest<List<Disk>>() {
            @Override
            public List<Disk> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.dto.Disk> disks;

                if (downloadAll) {
                    if (version.isV3Api()) {
                        disks = restClient.getDisksV3();
                    } else {
                        disks = restClient.getDisksV4();
                    }
                } else {
                    VersionSupport.VM_DISKS.throwIfNotSupported(version);

                    if (version.isV3Api()) {
                        disks = restClient.getDisksV3(realVmId);
                    } else {
                        disks = restClient.getDisksV4(realVmId);
                    }
                }

                return mapToEntities(disks);
            }
        };
    }

    public Request<List<Cluster>> getClustersRequest() {
        return new RestClientRequest<List<Cluster>>() {
            @Override
            public List<Cluster> fire() {
                if (version.isV3Api()) {
                    return mapToEntities(restClient.getClustersV3());
                }
                return mapToEntities(restClient.getClustersV4());
            }
        };
    }

    public Request<List<DataCenter>> getDataCentersRequest() {
        return new RestClientRequest<List<DataCenter>>() {
            @Override
            public List<DataCenter> fire() {
                if (version.isV3Api()) {
                    return mapToEntities(restClient.getDataCentersV3());
                }
                return mapToEntities(restClient.getDataCentersV4());
            }
        };
    }

    public Request<List<SnapshotNic>> getSnapshotNicsRequest(final String vmId, final String snapshotId) {
        final String realVmId = IdHelper.getIdPart(vmId);
        final String realSnapshotId = IdHelper.getIdPart(snapshotId);

        return new RestClientRequest<List<SnapshotNic>>() {
            @Override
            public List<SnapshotNic> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.dto.SnapshotNic> snapshotNics;

                if (version.isV3Api()) {
                    snapshotNics = restClient.getSnapshotNicsV3(realVmId, realSnapshotId);
                } else {
                    snapshotNics = restClient.getSnapshotNicsV4(realVmId, realSnapshotId);
                }

                if (snapshotNics != null) {
                    for (org.ovirt.mobile.movirt.rest.dto.SnapshotNic nic : snapshotNics.getList()) {
                        nic.vmId = realVmId;
                        nic.snapshotId = realSnapshotId;
                    }
                }

                return mapToEntities(snapshotNics);
            }
        };
    }

    public Request<List<Nic>> getNicsRequest(final String vmId) {
        final String realVmId = IdHelper.getIdPart(vmId);

        return new RestClientRequest<List<Nic>>() {
            @Override
            public List<Nic> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.dto.Nic> nics;

                if (version.isV3Api()) {
                    nics = restClient.getNicsV3(realVmId);
                } else {
                    nics = restClient.getNicsV4(realVmId);
                }

                return mapToEntities(nics);
            }
        };
    }

    public Request<List<Host>> getHostsRequest() {
        return new RestClientRequest<List<Host>>() {
            @Override
            public List<Host> fire() {
                if (version.isV3Api()) {
                    return mapToEntities(restClient.getHostsV3());
                }
                return mapToEntities(restClient.getHostsV4());
            }
        };
    }

    public Request<List<Vm>> getVmsRequest() {

        return new RestClientRequest<List<Vm>>() {
            @Override
            public List<Vm> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.dto.Vm> vms;

                if (propertiesManager.hasAdminPermissions()) {
                    int maxVms = sharedPreferencesHelper.getMaxVms();
                    String query = sharedPreferencesHelper.getStringPref(SettingsKey.VMS_SEARCH_QUERY);
                    if (StringUtils.isEmpty(query)) {
                        vms = version.isV3Api() ? restClient.getVmsV3(maxVms) :
                                restClient.getVmsV4(maxVms);
                    } else {
                        vms = version.isV3Api() ? restClient.getVmsV3(query, maxVms) :
                                restClient.getVmsV4(query, maxVms);
                    }
                } else {
                    vms = version.isV3Api() ? restClient.getVmsV3(-1) :
                            restClient.getVmsV4(-1);
                }

                return mapToEntities(vms);
            }
        };
    }

    public Request<List<StorageDomain>> getStorageDomainsRequest() {
        return new RestClientRequest<List<StorageDomain>>() {
            @Override
            public List<StorageDomain> fire() {
                if (version.isV3Api()) {
                    return mapToEntities(restClient.getStorageDomainsV3());
                }
                return mapToEntities(restClient.getStorageDomainsV4());
            }
        };
    }

    public Request<List<Snapshot>> getSnapshotsRequest(final String vmId) {
        final String realVmId = IdHelper.getIdPart(vmId);

        return new RestClientRequest<List<Snapshot>>() {
            @Override
            public List<Snapshot> fire() {
                RestEntityWrapperList<? extends org.ovirt.mobile.movirt.rest.dto.Snapshot> snapshots;

                if (version.isV3Api()) {
                    snapshots = restClient.getSnapshotsV3(realVmId);
                } else {
                    snapshots = restClient.getSnapshotsV4(realVmId);
                }

                if (snapshots != null) {
                    for (org.ovirt.mobile.movirt.rest.dto.Snapshot snapshot : snapshots.getList()) {
                        snapshot.vmId = realVmId; // Active VM Snapshot doesn't include this
                    }
                }

                return mapToEntities(snapshots);
            }
        };
    }

    public Request<Snapshot> getSnapshotRequest(final String vmId, final String snapshotId) {
        final String realVmId = IdHelper.getIdPart(vmId);
        final String realSnapshotId = IdHelper.getIdPart(snapshotId);

        return new RestClientRequest<Snapshot>() {
            @Override
            public Snapshot fire() {
                org.ovirt.mobile.movirt.rest.dto.Snapshot snapshot;

                if (version.isV3Api()) {
                    snapshot = restClient.getSnapshotV3(realVmId, realSnapshotId);
                } else {
                    snapshot = restClient.getSnapshotV4(realVmId, realSnapshotId);
                }

                snapshot.vmId = realVmId; // Active VM Snapshot doesn't include this

                return snapshot.toEntity(accountId);
            }
        };
    }

    public Request<List<Console>> getConsolesRequest(final String vmId) {
        return new RestClientRequest<List<Console>>() {
            @Override
            public List<Console> fire() {
                return mapToEntities(restClient.getConsoles(IdHelper.getIdPart(vmId)));
            }
        };
    }

    public Request<List<Event>> getEventsRequest() {
        return new RestClientRequest<List<Event>>() {
            @Override
            public List<Event> fire() {
                final int lastEventId = providerFacade.query(Event.class)
                        .where(OVirtContract.Event.ACCOUNT_ID, accountId)
                        .max(OVirtContract.Event.SHORT_ID)
                        .asAggregateResult();
                final String lastStrEventId = Integer.toString(lastEventId);

                int maxEventsPolled = sharedPreferencesHelper.getMaxEventsPolled();
                Events loadedEvents;

                if (propertiesManager.hasAdminPermissions()) {

                    String query = sharedPreferencesHelper.getStringPref(SettingsKey.EVENTS_SEARCH_QUERY);
                    if (!"".equals(query)) {
                        loadedEvents = restClient.getEventsSince(lastStrEventId, query, maxEventsPolled);
                    } else {
                        loadedEvents = restClient.getEventsSince(lastStrEventId, maxEventsPolled);
                    }
                } else {
                    loadedEvents = restClient.getEventsSince(lastStrEventId, -1);

                    // user polls all events, so remove excessive events to emulate admin's behavior
                    if (loadedEvents != null && maxEventsPolled < loadedEvents.getList().size()) {
                        Map<Integer, org.ovirt.mobile.movirt.rest.dto.Event> sortedEvents = new TreeMap<>(Collections.<Integer>reverseOrder());
                        for (org.ovirt.mobile.movirt.rest.dto.Event event : loadedEvents.getList()) {
                            sortedEvents.put(event.id, event);
                        }

                        loadedEvents.setList(new ArrayList<>(sortedEvents.values()).subList(0, maxEventsPolled - 1));
                    }
                }

                if (loadedEvents == null) {
                    return Collections.emptyList();
                }

                return mapToEntities(loadedEvents, entity -> entity.id > lastEventId);
            }
        };
    }

    public Request<List<Event>> getHostEventsRequest(final String hostName) {
        return new RestClientRequest<List<Event>>() {
            @Override
            public List<Event> fire() {
                if (propertiesManager.hasAdminPermissions()) {
                    return mapToEntities(restClient.getHostEvents(hostName));
                }
                return Collections.emptyList();
            }
        };
    }

    public Request<List<Event>> getVmEventsRequest(final String vmName) {
        return new RestClientRequest<List<Event>>() {
            @Override
            public List<Event> fire() {
                if (propertiesManager.hasAdminPermissions()) {
                    return mapToEntities(restClient.getVmEvents(vmName));
                }
                return Collections.emptyList();
            }
        };
    }

    public Request<List<Event>> getStorageDomainEventsRequest(final String storageDomainName) {
        return new RestClientRequest<List<Event>>() {
            @Override
            public List<Event> fire() {
                if (propertiesManager.hasAdminPermissions()) {
                    return mapToEntities(restClient.getStorageDomainEvents(storageDomainName));
                }
                return Collections.emptyList();
            }
        };
    }

    private <E, U extends RestEntityWrapper<E>> List<E> mapToEntities(RestEntityWrapperList<U> wrappersList) {
        return mapToEntities(wrappersList, null);
    }

    private <E, U extends RestEntityWrapper<E>> List<E> mapToEntities(RestEntityWrapperList<U> wrappersList, WrapPredicate<U> predicate) {
        if (wrappersList == null) {
            return Collections.emptyList();
        }

        List<U> wrappers = wrappersList.getList();

        if (wrappers == null) {
            return Collections.emptyList();
        }

        List<E> entities = new ArrayList<>();
        for (U rest : wrappers) {
            try {
                if (predicate == null || predicate.toWrap(rest)) {
                    entities.add(rest.toEntity(accountId));
                }
            } catch (Exception e) {
                // showing only as a toast since this problem may persist and we don't want to flood the user with messages like this as dialogs...
                messageHelper.showToast("Error parsing rest response, ignoring: " + rest.toString() + " error: " + e.getMessage());
            }
        }
        return entities;
    }

    private interface WrapPredicate<E> {
        boolean toWrap(E entity);
    }

    private abstract class RestClientRequest<T> implements Request<T> {
        @Override
        @SuppressWarnings("unchecked")
        public <U extends RestClientRootUrl & RestClientHeaders & RestClientSupport> U getRestClient() {
            return (U) restClient;
        }
    }
}
