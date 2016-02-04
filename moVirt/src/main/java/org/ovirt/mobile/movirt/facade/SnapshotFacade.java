package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.android.internal.util.Predicate;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.*;

@EBean
public class SnapshotFacade extends BaseEntityFacade<Snapshot> {

    public SnapshotFacade() {
        super(Snapshot.class);
    }

    @Override
    public Intent getDetailIntent(Snapshot entity, Context context) {
//        Intent intent = new Intent(context, SnapshotDetailActivity_.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.setData(entity.getUri());
//        return intent;

        return null; // remove after snapshot detail finished
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
    protected OVirtClient.CompositeResponse<List<Snapshot>> getSyncAllResponse(final OVirtClient.Response<List<Snapshot>> response, final String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return new OVirtClient.CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(Snapshot.class, new VmIdPredicate<Snapshot>(vmId)), response);
    }
}
