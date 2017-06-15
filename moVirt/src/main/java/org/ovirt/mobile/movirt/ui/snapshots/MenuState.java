package org.ovirt.mobile.movirt.ui.snapshots;

import org.ovirt.mobile.movirt.model.enums.SnapshotStatus;
import org.ovirt.mobile.movirt.model.enums.VmStatus;

public class MenuState {
    public static final MenuState EMPTY = new MenuState(null, null, false);
    public final VmStatus vmStatus;
    public final SnapshotStatus snapshotStatus;
    public final boolean allSnapshotsOK;

    public MenuState(VmStatus vmStatus, SnapshotStatus snapshotStatus, boolean allSnapshotsOK) {
        this.vmStatus = vmStatus;
        this.snapshotStatus = snapshotStatus;
        this.allSnapshotsOK = allSnapshotsOK;
    }

    public boolean hasVmStatus() {
        return vmStatus != null;
    }

    public boolean hasSnapshotStatus() {
        return snapshotStatus != null;
    }

    public boolean hasBothStatuses() {
        return vmStatus != null && snapshotStatus != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuState)) return false;

        MenuState menuState = (MenuState) o;

        if (allSnapshotsOK != menuState.allSnapshotsOK) return false;
        if (vmStatus != menuState.vmStatus) return false;
        return snapshotStatus == menuState.snapshotStatus;
    }

    @Override
    public int hashCode() {
        int result = vmStatus != null ? vmStatus.hashCode() : 0;
        result = 31 * result + (snapshotStatus != null ? snapshotStatus.hashCode() : 0);
        result = 31 * result + (allSnapshotsOK ? 1 : 0);
        return result;
    }
}
