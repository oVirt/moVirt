package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.VmIdSnapshotIdPredicate;
import org.ovirt.mobile.movirt.model.SnapshotNic;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class SnapshotNicFacade extends BaseEntityFacade<SnapshotNic> {

    public SnapshotNicFacade() {
        super(SnapshotNic.class);
    }

    @Override
    public Intent getDetailIntent(SnapshotNic entity, Context context) {
        return null;
    }

    @Override
    protected Request<SnapshotNic> getSyncOneRestRequest(String nicId, String... ids) {
        throw new UnsupportedOperationException();
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

        return new CompositeResponse<>(new SimpleResponse<List<SnapshotNic>>() {
            @Override
            public void onResponse(List<SnapshotNic> nics) throws RemoteException {
                syncAdapter.updateLocalEntities(nics, SnapshotNic.class,
                        new VmIdSnapshotIdPredicate<SnapshotNic>(vmId, snapshotId));
            }
        }, response);
    }
}
