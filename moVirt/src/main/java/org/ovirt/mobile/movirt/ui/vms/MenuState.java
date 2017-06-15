package org.ovirt.mobile.movirt.ui.vms;

import org.ovirt.mobile.movirt.model.enums.VmStatus;

public class MenuState {
    public static final MenuState EMPTY = new MenuState(null, false, false, false);
    public final VmStatus status;
    public final boolean createSnapshotVisibility;
    public final boolean hasSpiceConsole;
    public final boolean hasVncConsole;

    public MenuState(VmStatus status, boolean createSnapshotVisibility, boolean hasSpiceConsole, boolean hasVncConsole) {
        this.status = status;
        this.createSnapshotVisibility = createSnapshotVisibility;
        this.hasSpiceConsole = hasSpiceConsole;
        this.hasVncConsole = hasVncConsole;
    }

    public boolean hasStatus() {
        return status != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuState)) return false;

        MenuState menuState = (MenuState) o;

        if (createSnapshotVisibility != menuState.createSnapshotVisibility) return false;
        if (hasSpiceConsole != menuState.hasSpiceConsole) return false;
        if (hasVncConsole != menuState.hasVncConsole) return false;
        return status == menuState.status;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (createSnapshotVisibility ? 1 : 0);
        result = 31 * result + (hasSpiceConsole ? 1 : 0);
        result = 31 * result + (hasVncConsole ? 1 : 0);
        return result;
    }
}
