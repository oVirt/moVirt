package org.ovirt.mobile.movirt.rest;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Delete;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.RequiresCookie;
import org.androidannotations.annotations.rest.RequiresHeader;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.annotations.rest.SetsCookie;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.androidannotations.api.rest.RestClientSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(converters = MappingJackson2HttpMessageConverter.class)
@Accept(MediaType.APPLICATION_JSON + "; detail=statistics+disks+nics")
@RequiresHeader({"Filter", "Accept-Encoding", "Session-TTL", "Prefer", "Version"})
@SetsCookie("JSESSIONID")
@RequiresCookie("JSESSIONID")
@RequiresAuthentication
public interface OVirtRestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    @Post("/vms/{vmId}/start")
    void startVm(Action action, String vmId);

    @Post("/vms/{vmId}/stop")
    void stopVm(Action action, String vmId);

    @Post("/vms/{vmId}/reboot")
    void rebootVm(Action action, String vmId);

    @Post("/vms/{vmId}/migrate")
    void migrateVmToHost(Action action, String vmId);

    @Post("/vms/{vmId}/cancelmigration")
    void cancelMigration(Action action, String vmId);

    @Post("/hosts/{hostId}/activate")
    void activateHost(Action action, String hostId);

    @Post("/hosts/{hostId}/deactivate")
    void deactivateHost(Action action, String hostId);

    @Delete("/vms/{vmId}/snapshots/{snapshotId}")
    void deleteSnapshot(String vmId, String snapshotId);

    @Post("/vms/{vmId}/snapshots/{snapshotId}/restore")
    void restoreSnapshot(SnapshotAction action, String vmId, String snapshotId);

    @Get("/events;max={maxToLoad}?from={lastEventId}&search=sortby time desc")
    Events getEventsSince(String lastEventId, int maxToLoad);

    @Get("/events;max={maxToLoad}?from={lastEventId}&search={query}")
    Events getEventsSince(String lastEventId, String query, int maxToLoad);

    @Post("/vms/{vmId}/ticket")
    ActionTicket getConsoleTicket(Action action, String vmId);

    void setCookie(String name, String value);

    String getCookie(String name);

    // API 3 and 4 methods

    @Post("/vms/{vmId}/preview_snapshot")
    void previewSnapshotV3(SnapshotAction snapshotAction, String vmId);

    @Post("/vms/{vmId}/previewsnapshot")
    void previewSnapshotV4(SnapshotAction snapshotAction, String vmId);

    @Post("/vms/{vmId}/undo_snapshot")
    void undoSnapshotV3(Action action, String vmId);

    @Post("/vms/{vmId}/undosnapshot")
    void undoSnapshotV4(Action action, String vmId);

    @Post("/vms/{vmId}/commit_snapshot")
    void commitSnapshotV3(Action action, String vmId);

    @Post("/vms/{vmId}/commitsnapshot")
    void commitSnapshotV4(Action action, String vmId);

    @Post("/vms/{vmId}/snapshots")
    void createSnapshot(Snapshot snapshot, String vmId);

    @Get("/vms;max={maxToLoad}")
    org.ovirt.mobile.movirt.rest.v3.Vms getVmsV3(int maxToLoad);

    @Get("/vms;max={maxToLoad}")
    org.ovirt.mobile.movirt.rest.v4.Vms getVmsV4(int maxToLoad);

    @Get("/vms;max={maxToLoad}?search={query}")
    org.ovirt.mobile.movirt.rest.v3.Vms getVmsV3(String query, int maxToLoad);

    @Get("/vms;max={maxToLoad}?search={query}")
    org.ovirt.mobile.movirt.rest.v4.Vms getVmsV4(String query, int maxToLoad);

    @Get("/clusters")
    org.ovirt.mobile.movirt.rest.v3.Clusters getClustersV3();

    @Get("/clusters")
    org.ovirt.mobile.movirt.rest.v4.Clusters getClustersV4();

    @Get("/datacenters")
    org.ovirt.mobile.movirt.rest.v3.DataCenters getDataCentersV3();

    @Get("/datacenters")
    org.ovirt.mobile.movirt.rest.v4.DataCenters getDataCentersV4();

    @Get("/storagedomains")
    org.ovirt.mobile.movirt.rest.v3.StorageDomains getStorageDomainsV3();

    @Get("/storagedomains")
    org.ovirt.mobile.movirt.rest.v4.StorageDomains getStorageDomainsV4();

    @Get("/storagedomains/{storageDomainId}")
    org.ovirt.mobile.movirt.rest.v3.StorageDomain getStorageDomainV3(String storageDomainId);

    @Get("/storagedomains/{storageDomainId}")
    org.ovirt.mobile.movirt.rest.v4.StorageDomain getStorageDomainV4(String storageDomainId);

    @Get("/vms/{vmId}")
    org.ovirt.mobile.movirt.rest.v3.Vm getVmV3(String vmId);

    @Get("/vms/{vmId}")
    org.ovirt.mobile.movirt.rest.v4.Vm getVmV4(String vmId);

    @Get("/vms/{vmId}/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v3.Disk getDiskV3(String vmId, String diskId);

    @Get("/vms/{vmId}/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v4.Disk getDiskV4(String vmId, String diskId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v3.Disk getDiskV3(String vmId, String snapshotId, String diskId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v4.Disk getDiskV4(String vmId, String snapshotId, String diskId);

    @Get("/vms/{vmId}/disks")
    org.ovirt.mobile.movirt.rest.v3.Disks getDisksV3(String vmId);

    @Get("/vms/{vmId}/disks")
    org.ovirt.mobile.movirt.rest.v4.Disks getDisksV4(String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks")
    org.ovirt.mobile.movirt.rest.v3.Disks getDisksV3(String vmId, String snapshotId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks")
    org.ovirt.mobile.movirt.rest.v4.Disks getDisksV4(String vmId, String snapshotId);

    @Get("/vms/{vmId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v3.Nic getNicV3(String vmId, String nicId);

    @Get("/vms/{vmId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v4.Nic getNicV4(String vmId, String nicId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v3.Nic getNicV3(String vmId, String snapshotId, String nicId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v4.Nic getNicV4(String vmId, String snapshotId, String nicId);

    @Get("/vms/{vmId}/nics")
    org.ovirt.mobile.movirt.rest.v3.Nics getNicsV3(String vmId);

    @Get("/vms/{vmId}/nics")
    org.ovirt.mobile.movirt.rest.v4.Nics getNicsV4(String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics")
    org.ovirt.mobile.movirt.rest.v3.Nics getNicsV3(String vmId, String snapshotId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics")
    org.ovirt.mobile.movirt.rest.v4.Nics getNicsV4(String vmId, String snapshotId);

    @Get("/hosts")
    org.ovirt.mobile.movirt.rest.v3.Hosts getHostsV3();

    @Get("/hosts")
    org.ovirt.mobile.movirt.rest.v4.Hosts getHostsV4();

    @Get("/hosts/{hostId}")
    org.ovirt.mobile.movirt.rest.v3.Host getHostV3(String hostId);

    @Get("/hosts/{hostId}")
    org.ovirt.mobile.movirt.rest.v4.Host getHostV4(String hostId);

    @Get("/vms/{vmId}/snapshots")
    org.ovirt.mobile.movirt.rest.v3.Snapshots getSnapshotsV3(String vmId);

    @Get("/vms/{vmId}/snapshots")
    org.ovirt.mobile.movirt.rest.v4.Snapshots getSnapshotsV4(String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}")
    org.ovirt.mobile.movirt.rest.v3.Snapshot getSnapshotV3(String vmId, String snapshotId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}")
    org.ovirt.mobile.movirt.rest.v4.Snapshot getSnapshotV4(String vmId, String snapshotId);
}
