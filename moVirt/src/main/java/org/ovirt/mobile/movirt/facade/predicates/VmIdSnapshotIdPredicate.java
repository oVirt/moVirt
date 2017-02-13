package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.provider.OVirtContract;

public class VmIdSnapshotIdPredicate<T extends OVirtContract.HasSnapshot & OVirtContract.HasVm> implements Predicate<T> {
    private String vmId;
    private String snapshotId;

    public VmIdSnapshotIdPredicate(String vmId, String snapshotsId) {
        if (vmId == null) {
            throw new IllegalArgumentException("vmId is null");
        }
        if (snapshotsId == null) {
            throw new IllegalArgumentException("snapshotsId is null");
        }
        this.vmId = vmId;
        this.snapshotId = snapshotsId;
    }

    @Override
    public boolean apply(T t) {
        return vmId.equals(t.getVmId()) && snapshotId.equals(t.getSnapshotId());
    }
}
