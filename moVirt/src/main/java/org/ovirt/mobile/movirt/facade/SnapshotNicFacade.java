package org.ovirt.mobile.movirt.facade;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.VmIdSnapshotIdPredicate;
import org.ovirt.mobile.movirt.model.SnapshotNic;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class SnapshotNicFacade extends BaseEntityFacade<SnapshotNic> {

    public SnapshotNicFacade() {
        super(SnapshotNic.class);
    }

    @Override
    protected Request<List<SnapshotNic>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId", "snapshotId");
        String vmId = ids[0];
        String snapshotId = ids[1];
        return oVirtClient.getSnapshotNicsRequest(vmId, snapshotId);
    }

    @Override
    protected CompositeResponse<List<SnapshotNic>> getSyncAllResponse(final Response<List<SnapshotNic>> response, final String... ids) {
        requireSignature(ids, "vmId", "snapshotId");
        final String vmId = ids[0];
        final String snapshotId = ids[1];

        return respond()
                .withScopePredicate(new VmIdSnapshotIdPredicate<>(vmId, snapshotId))
                .asUpdateEntitiesResponse()
                .addResponse(response);
    }
}
