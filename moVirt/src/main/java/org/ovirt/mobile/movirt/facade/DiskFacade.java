package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;

import com.android.internal.util.Predicate;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.AndPredicate;
import org.ovirt.mobile.movirt.facade.predicates.NotSnapshotEmbeddedPredicate;
import org.ovirt.mobile.movirt.facade.predicates.SnapshotIdPredicate;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
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
        if (ids.length == 2) {
            String vmId = ids[0];
            String snapshotId = ids[1];
            return oVirtClient.getDiskRequest(vmId, snapshotId, diskId);
        } else {
            requireSignature(ids, "vmId");
            String vmId = ids[0];
            return oVirtClient.getDiskRequest(vmId, diskId);
        }
    }

    @Override
    protected OVirtClient.Request<List<Disk>> getSyncAllRestRequest(String... ids) {
        if (ids.length == 2) {
            String vmId = ids[0];
            String snapshotId = ids[1];
            return oVirtClient.getDisksRequest(vmId, snapshotId);
        } else {
            requireSignature(ids, "vmId");
            String vmId = ids[0];
            return oVirtClient.getDisksRequest(vmId);
        }
    }

    @Override
    protected OVirtClient.CompositeResponse<List<Disk>> getSyncAllResponse(final OVirtClient.Response<List<Disk>> response, final String... ids) {
        Predicate<Disk> predicate;
        if (ids.length == 2) {
            String snapshotId = ids[1];
            predicate = new SnapshotIdPredicate<>(snapshotId);
        } else {
            requireSignature(ids, "vmId");
            String vmId = ids[0];
            predicate = new AndPredicate<>(new VmIdPredicate<Disk>(vmId), new NotSnapshotEmbeddedPredicate<Disk>());
        }

        return new OVirtClient.CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(Disk.class, predicate), response);
    }
}
