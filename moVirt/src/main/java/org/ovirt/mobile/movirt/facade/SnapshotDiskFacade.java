package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.VmIdSnapshotIdPredicate;
import org.ovirt.mobile.movirt.model.SnapshotDisk;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class SnapshotDiskFacade extends BaseEntityFacade<SnapshotDisk> {

    public SnapshotDiskFacade() {
        super(SnapshotDisk.class);
    }

    @Override
    public Intent getDetailIntent(SnapshotDisk entity, Context context) {
        return null;
    }

    @Override
    protected Request<SnapshotDisk> getSyncOneRestRequest(String diskId, String... ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Request<List<SnapshotDisk>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId", "snapshotId");
        String vmId = ids[0];
        String snapshotId = ids[1];

        return oVirtClient.getSnapshotDisksRequest(vmId, snapshotId);
    }

    @Override
    protected CompositeResponse<List<SnapshotDisk>> getSyncAllResponse(final Response<List<SnapshotDisk>> response, final String... ids) {
        requireSignature(ids, "vmId", "snapshotId");
        final String vmId = ids[0];
        final String snapshotId = ids[1];

        return new CompositeResponse<>(response, syncAdapter.getUpdateEntitiesResponse(SnapshotDisk.class,
                new VmIdSnapshotIdPredicate<SnapshotDisk>(vmId, snapshotId)));
    }
}
