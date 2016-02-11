package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.SnapshotsIdPredicate;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.rest.OVirtClient.CompositeResponse;
import org.ovirt.mobile.movirt.rest.OVirtClient.SimpleResponse;
import org.ovirt.mobile.movirt.ui.snapshots.SnapshotDetailActivity_;

import java.util.ArrayList;
import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class SnapshotFacade extends BaseEntityFacade<Snapshot> {

    public SnapshotFacade() {
        super(Snapshot.class);
    }

    @Override
    public Intent getDetailIntent(Snapshot entity, Context context) {
        Intent intent = null;
        if (entity.getType() != Snapshot.SnapshotType.ACTIVE) {
            intent = new Intent(context, SnapshotDetailActivity_.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setData(entity.getUri());
        }
        return intent;
    }

    @Override
    protected OVirtClient.Request<Snapshot> getSyncOneRestRequest(String snapshotId, String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getSnapshotRequest(vmId, snapshotId);
    }

    @Override
    protected OVirtClient.Request<List<Snapshot>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getSnapshotsRequest(vmId);
    }

    @Override
    protected CompositeResponse<List<Snapshot>> getSyncAllResponse(final OVirtClient.Response<List<Snapshot>> response, final String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];

        CompositeResponse<List<Snapshot>> res = new CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(Snapshot.class, new VmIdPredicate<Snapshot>(vmId)));
        res.addResponse(new SimpleResponse<List<Snapshot>>() {
            @Override
            public void onResponse(List<Snapshot> snapshots) throws RemoteException {
                List<Vm> vms = new ArrayList<>();
                for (Snapshot snapshot : snapshots) {
                    Vm vm = snapshot.getVm();
                    if (vm != null) {
                        vm.setSnapshotId(snapshot.getId());
                        vms.add(vm);
                    }
                }
                syncAdapter.updateLocalEntities(vms, Vm.class, new SnapshotsIdPredicate<Vm>(snapshots));
            }
        });
        res.addResponse(response);

        return res;
    }
}
