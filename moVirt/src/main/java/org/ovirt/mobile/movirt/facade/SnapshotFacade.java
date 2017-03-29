package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.SnapshotVm;
import org.ovirt.mobile.movirt.model.enums.SnapshotType;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
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
        if (entity.getType() != SnapshotType.ACTIVE) {
            intent = new Intent(context, SnapshotDetailActivity_.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setData(entity.getUri());
            intent.putExtra(OVirtContract.HasVm.VM_ID, entity.getVmId());
        }
        return intent;
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
                if (vm != null) {
                    syncAdapter.updateLocalEntity(vm, SnapshotVm.class);
                }
            }
        });
        return res;
    }

    @Override
    protected CompositeResponse<List<Snapshot>> getSyncAllResponse(final Response<List<Snapshot>> response, final String... ids) {
        requireSignature(ids, "vmId");
        final String vmId = ids[0];

        CompositeResponse<List<Snapshot>> res = new CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(Snapshot.class, new VmIdPredicate<Snapshot>(vmId)));
        res.addResponse(new SimpleResponse<List<Snapshot>>() {
            @Override
            public void onResponse(List<Snapshot> snapshots) throws RemoteException {
                List<SnapshotVm> vms = new ArrayList<>();
                for (Snapshot snapshot : snapshots) {
                    SnapshotVm vm = snapshot.getVm();
                    if (vm != null) {
                        vms.add(vm);
                    }
                }
                syncAdapter.updateLocalEntities(vms, SnapshotVm.class, new VmIdPredicate<SnapshotVm>(vmId));
            }
        });
        res.addResponse(response);

        return res;
    }
}
