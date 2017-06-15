package org.ovirt.mobile.movirt.facade;

import android.os.RemoteException;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.SnapshotVm;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;

import java.util.ArrayList;
import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class SnapshotFacade extends BaseEntityFacade<Snapshot> {

    public SnapshotFacade() {
        super(Snapshot.class);
    }

    @Override
    protected Request<Snapshot> getSyncOneRestRequest(String snapshotId, String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getSnapshotRequest(vmId, snapshotId);
    }

    @Override
    protected Request<List<Snapshot>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getSnapshotsRequest(vmId);
    }

    @Override
    protected CompositeResponse<Snapshot> getSyncOneResponse(final Response<Snapshot> response, final String... ids) {
        requireSignature(ids, "vmId");

        CompositeResponse<Snapshot> res = super.getSyncOneResponse(response);
        res.addResponse(new SimpleResponse<Snapshot>() {
            @Override
            public void onResponse(Snapshot snapshot) throws RemoteException {
                SnapshotVm vm = snapshot.getVm();
                respond(SnapshotVm.class).updateEntity(vm);
            }
        });
        return res;
    }

    @Override
    protected CompositeResponse<List<Snapshot>> getSyncAllResponse(final Response<List<Snapshot>> response, final String... ids) {
        requireSignature(ids, "vmId");
        final String vmId = ids[0];

        return respond()
                .withScopePredicate(new VmIdPredicate<>(vmId))
                .asUpdateEntitiesResponse()
                .addResponse(new SimpleResponse<List<Snapshot>>() {
                    @Override
                    public void onResponse(List<Snapshot> snapshots) throws RemoteException {
                        List<SnapshotVm> vms = new ArrayList<>();
                        for (Snapshot snapshot : snapshots) {
                            SnapshotVm vm = snapshot.getVm();
                            if (vm != null) {
                                vms.add(vm);
                            }
                        }
                        respond(SnapshotVm.class)
                                .withScopePredicate(new VmIdPredicate<>(vmId))
                                .updateEntities(vms);
                    }
                })
                .addResponse(response);
    }
}
