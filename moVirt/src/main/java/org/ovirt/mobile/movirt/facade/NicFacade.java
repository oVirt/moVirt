package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.android.internal.util.Predicate;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.AndPredicate;
import org.ovirt.mobile.movirt.facade.predicates.NotSnapshotEmbeddedPredicate;
import org.ovirt.mobile.movirt.facade.predicates.SnapshotIdPredicate;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class NicFacade extends BaseEntityFacade<Nic> {

    public NicFacade() {
        super(Nic.class);
    }

    @Override
    public Intent getDetailIntent(Nic entity, Context context) {
        return null;
    }

    @Override
    protected Request<Nic> getSyncOneRestRequest(String nicId, String... ids) {
        if (ids.length == 2) {
            String vmId = ids[0];
            String snapshotId = ids[1];
            return oVirtClient.getNicRequest(vmId, snapshotId, nicId);
        } else {
            requireSignature(ids, "vmId");
            String vmId = ids[0];
            return oVirtClient.getNicRequest(vmId, nicId);
        }
    }

    @Override
    protected Request<List<Nic>> getSyncAllRestRequest(String... ids) {
        if (ids.length == 2) {
            String vmId = ids[0];
            String snapshotId = ids[1];
            return oVirtClient.getNicsRequest(vmId, snapshotId);
        } else {
            requireSignature(ids, "vmId");
            String vmId = ids[0];
            return oVirtClient.getNicsRequest(vmId);
        }
    }

    @Override
    protected CompositeResponse<List<Nic>> getSyncAllResponse(final Response<List<Nic>> response, final String... ids) {
        if (ids.length == 2) {
            final String snapshotId = ids[1];

            return new CompositeResponse<>(new SimpleResponse<List<Nic>>() {
                @Override
                public void onResponse(List<Nic> nics) throws RemoteException {
                    for (Nic nic : nics) {
                        nic.setSnapshotId(snapshotId);
                    }
                    syncAdapter.updateLocalEntities(nics, Nic.class, new SnapshotIdPredicate<Nic>(snapshotId));
                }
            }, response);
        } else {
            requireSignature(ids, "vmId");
            String vmId = ids[0];
            Predicate<Nic> predicate = new AndPredicate<>(new VmIdPredicate<Nic>(vmId),
                    new NotSnapshotEmbeddedPredicate<Nic>());

            return new CompositeResponse<>(
                    syncAdapter.getUpdateEntitiesResponse(Nic.class, predicate), response);
        }
    }
}
