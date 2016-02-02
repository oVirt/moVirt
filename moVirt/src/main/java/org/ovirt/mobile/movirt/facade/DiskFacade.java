package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class DiskFacade extends BaseEntityFacade<Disk> {

    public DiskFacade() {
        super(Disk.class);
    }

    @Override
    public Intent getDetailIntent(Disk entity, Context context) {
        return null;
    }

    @Override
    protected OVirtClient.Request<Disk> getSyncOneRestRequest(String diskId, String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getDiskRequest(vmId, diskId);
    }

    @Override
    protected OVirtClient.Request<List<Disk>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getDisksRequest(vmId);
    }

    @Override
    protected OVirtClient.CompositeResponse<List<Disk>> getSyncAllResponse(final OVirtClient.Response<List<Disk>> response, final String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return new OVirtClient.CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(Disk.class, new VmIdPredicate<Disk>(vmId)), response);
    }
}
