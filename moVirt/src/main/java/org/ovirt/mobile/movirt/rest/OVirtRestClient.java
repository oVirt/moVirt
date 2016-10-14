package org.ovirt.mobile.movirt.rest;

import org.androidannotations.rest.spring.annotations.Accept;
import org.androidannotations.rest.spring.annotations.Body;
import org.androidannotations.rest.spring.annotations.Delete;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.RequiresAuthentication;
import org.androidannotations.rest.spring.annotations.RequiresCookie;
import org.androidannotations.rest.spring.annotations.RequiresHeader;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.annotations.SetsCookie;
import org.androidannotations.rest.spring.api.MediaType;
import org.androidannotations.rest.spring.api.RestClientHeaders;
import org.androidannotations.rest.spring.api.RestClientRootUrl;
import org.androidannotations.rest.spring.api.RestClientSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(converters = MappingJackson2HttpMessageConverter.class)
@Accept(MediaType.APPLICATION_JSON + "; detail=statistics+disks+nics")
// disks and nics work only in v3 API
@RequiresHeader({"Filter", "Accept-Encoding", "Session-TTL", "Prefer", "Version"})
@SetsCookie("JSESSIONID")
@RequiresCookie("JSESSIONID")
@RequiresAuthentication
public interface OVirtRestClient extends RestClientRootUrl, RestClientHeaders, RestClientSupport {

    @Post("/vms/{vmId}/start")
    void startVm(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/stop")
    void stopVm(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/reboot")
    void rebootVm(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/migrate")
    void migrateVmToHost(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/cancelmigration")
    void cancelMigration(@Body Action action, @Path String vmId);

    @Post("/hosts/{hostId}/activate")
    void activateHost(@Body Action action, @Path String hostId);

    @Post("/hosts/{hostId}/deactivate")
    void deactivateHost(@Body Action action, @Path String hostId);

    @Delete("/vms/{vmId}/snapshots/{snapshotId}")
    void deleteSnapshot(@Path String vmId, @Path String snapshotId);

    @Post("/vms/{vmId}/snapshots/{snapshotId}/restore")
    void restoreSnapshot(@Body SnapshotAction action, @Path String vmId, @Path String snapshotId);

    @Get("/events;max={maxToLoad}?from={lastEventId}&search=sortby time desc")
    Events getEventsSince(@Path String lastEventId, @Path int maxToLoad);

    @Get("/events;max={maxToLoad}?from={lastEventId}&search={query}")
    Events getEventsSince(@Path String lastEventId, @Path String query, @Path int maxToLoad);

    @Get("/vms/{vmId}/graphicsconsoles")
    Consoles getConsoles(@Path String vmId);

    void setCookie(String name, String value);

    String getCookie(String name);

    // API 3 and 4 methods

    @Post("/vms/{vmId}/preview_snapshot")
    void previewSnapshotV3(@Body SnapshotAction snapshotAction, @Path String vmId);

    @Post("/vms/{vmId}/previewsnapshot")
    void previewSnapshotV4(@Body SnapshotAction snapshotAction, @Path String vmId);

    @Post("/vms/{vmId}/undo_snapshot")
    void undoSnapshotV3(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/undosnapshot")
    void undoSnapshotV4(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/commit_snapshot")
    void commitSnapshotV3(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/commitsnapshot")
    void commitSnapshotV4(@Body Action action, @Path String vmId);

    @Post("/vms/{vmId}/snapshots")
    void createSnapshot(@Body Snapshot snapshot, @Path String vmId);

    @Get("/vms;max={maxToLoad}")
    org.ovirt.mobile.movirt.rest.v3.Vms getVmsV3(@Path int maxToLoad);

    @Get("/vms;max={maxToLoad}")
    org.ovirt.mobile.movirt.rest.v4.Vms getVmsV4(@Path int maxToLoad);

    @Get("/vms;max={maxToLoad}?search={query}")
    org.ovirt.mobile.movirt.rest.v3.Vms getVmsV3(@Path String query, @Path int maxToLoad);

    @Get("/vms;max={maxToLoad}?search={query}")
    org.ovirt.mobile.movirt.rest.v4.Vms getVmsV4(@Path String query, @Path int maxToLoad);

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
    org.ovirt.mobile.movirt.rest.v3.StorageDomain getStorageDomainV3(@Path String storageDomainId);

    @Get("/storagedomains/{storageDomainId}")
    org.ovirt.mobile.movirt.rest.v4.StorageDomain getStorageDomainV4(@Path String storageDomainId);

    @Get("/vms/{vmId}")
    org.ovirt.mobile.movirt.rest.v3.Vm getVmV3(@Path String vmId);

    @Get("/vms/{vmId}")
    org.ovirt.mobile.movirt.rest.v4.Vm getVmV4(@Path String vmId);

    @Get("/vms/{vmId}/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v3.Disk getDiskV3(@Path String vmId, @Path String diskId);

    @Get("/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v4.Disk getDiskV4(@Path String diskId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v3.Disk getDiskV3(@Path String vmId, @Path String snapshotId, @Path String diskId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks/{diskId}")
    org.ovirt.mobile.movirt.rest.v4.Disk getDiskV4(@Path String vmId, @Path String snapshotId, @Path String diskId);

    @Get("/vms/{vmId}/disks")
    org.ovirt.mobile.movirt.rest.v3.Disks getDisksV3(@Path String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks")
    org.ovirt.mobile.movirt.rest.v3.Disks getDisksV3(@Path String vmId, @Path String snapshotId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/disks")
    org.ovirt.mobile.movirt.rest.v4.Disks getDisksV4(@Path String vmId, @Path String snapshotId);

    @Get("/vms/{vmId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v3.Nic getNicV3(@Path String vmId, @Path String nicId);

    @Get("/vms/{vmId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v4.Nic getNicV4(@Path String vmId, @Path String nicId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v3.Nic getNicV3(@Path String vmId, @Path String snapshotId, @Path String nicId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics/{nicId}")
    org.ovirt.mobile.movirt.rest.v4.Nic getNicV4(@Path String vmId, @Path String snapshotId, @Path String nicId);

    @Get("/vms/{vmId}/nics")
    org.ovirt.mobile.movirt.rest.v3.Nics getNicsV3(@Path String vmId);

    @Get("/vms/{vmId}/nics")
    org.ovirt.mobile.movirt.rest.v4.Nics getNicsV4(@Path String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics")
    org.ovirt.mobile.movirt.rest.v3.Nics getNicsV3(@Path String vmId, @Path String snapshotId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}/nics")
    org.ovirt.mobile.movirt.rest.v4.Nics getNicsV4(@Path String vmId, @Path String snapshotId);

    @Get("/hosts")
    org.ovirt.mobile.movirt.rest.v3.Hosts getHostsV3();

    @Get("/hosts")
    org.ovirt.mobile.movirt.rest.v4.Hosts getHostsV4();

    @Get("/hosts/{hostId}")
    org.ovirt.mobile.movirt.rest.v3.Host getHostV3(@Path String hostId);

    @Get("/hosts/{hostId}")
    org.ovirt.mobile.movirt.rest.v4.Host getHostV4(@Path String hostId);

    @Get("/vms/{vmId}/snapshots")
    org.ovirt.mobile.movirt.rest.v3.Snapshots getSnapshotsV3(@Path String vmId);

    @Get("/vms/{vmId}/snapshots")
    org.ovirt.mobile.movirt.rest.v4.Snapshots getSnapshotsV4(@Path String vmId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}")
    org.ovirt.mobile.movirt.rest.v3.Snapshot getSnapshotV3(@Path String vmId, @Path String snapshotId);

    @Get("/vms/{vmId}/snapshots/{snapshotId}")
    org.ovirt.mobile.movirt.rest.v4.Snapshot getSnapshotV4(@Path String vmId, @Path String snapshotId);
}
