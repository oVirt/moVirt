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
@RequiresHeader({"Filter", "Accept-Encoding", "Session-TTL", "Prefer"})
@SetsCookie("JSESSIONID")
@RequiresCookie("JSESSIONID")
@RequiresAuthentication
public interface OVirtRestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    @Get("/vms;max={maxToLoad}")
    Vms getVms(int maxToLoad);

    @Get("/vms;max={maxToLoad}?search={query}")
    Vms getVms(String query, int maxToLoad);

    @Get("/vms/{vmId}/statistics")
    Statistics getVmStatistics(String vmId);

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

    @Post("/vms/{vmId}/snapshots")
    void createSnapshot(Snapshot snapshot, String vmId);

    @Delete("/vms/{vmId}/snapshots/{snapshotId}")
    void deleteSnapshot(String vmId, String snapshotId);

    @Post("/vms/{vmId}/preview_snapshot")
    void previewSnapshot(SnapshotAction snapshotAction, String vmId);

    @Post("/vms/{vmId}/undo_snapshot")
    void undoSnapshot(Action action, String vmId);

    @Post("/vms/{vmId}/commit_snapshot")
    void commitSnapshot(Action action, String vmId);

    @Post("/vms/{vmId}/snapshots/{snapshotId}/restore")
    void restoreSnapshot(SnapshotAction action, String vmId, String snapshotId);

    @Get("/clusters")
    Clusters getClusters();

    @Get("/datacenters")
    DataCenters getDataCenters();

    @Get("/storagedomains")
    StorageDomains getStorageDomains();

    @Get("/storagedomains/{storageDomainId}")
    StorageDomain getStorageDomain(String storageDomainId);

    @Get("/events;max={maxToLoad}?from={lastEventId}&search=sortby time desc")
    Events getEventsSince(String lastEventId, int maxToLoad);

    @Get("/events;max={maxToLoad}?from={lastEventId}&search={query}")
    Events getEventsSince(String lastEventId, String query, int maxToLoad);

    @Get("/vms/{vmId}")
    Vm getVm(String vmId);

    @Post("/vms/{vmId}/ticket")
    ActionTicket getConsoleTicket(Action action, String vmId);

    @Get("/vms/{vmId}/disks/{diskId}")
    Disk getDisk(String vmId, String diskId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks/{diskId}")
    Disk getDisk(String vmId, String snapshotId, String diskId);

    @Get("/vms/{vmId}/disks")
    Disks getDisks(String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks")
    Disks getDisks(String vmId, String snapshotId);

    @Get("/vms/{vmId}/nics/{nicId}")
    Nic getNic(String vmId, String nicId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics/{nicId}")
    Nic getNic(String vmId, String snapshotId, String nicId);

    @Get("/vms/{vmId}/nics")
    Nics getNics(String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics")
    Nics getNics(String vmId, String snapshotId);

    @Get("/hosts")
    Hosts getHosts();

    @Get("/hosts/{hostId}")
    Host getHost(String hostId);

    @Get("/vms/{vmId}/snapshots")
    Snapshots getSnapshots(String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}")
    Snapshot getSnapshot(String vmId, String snapshotId);

    @Get("/")
    EmptyResult login();

    void setCookie(String name, String value);

    String getCookie(String name);

}
