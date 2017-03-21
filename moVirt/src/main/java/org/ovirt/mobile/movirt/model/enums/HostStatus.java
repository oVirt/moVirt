package org.ovirt.mobile.movirt.model.enums;

import org.ovirt.mobile.movirt.R;

public enum HostStatus {
    DOWN(R.drawable.down),
    ERROR(R.drawable.error),
    INITIALIZING(R.drawable.wait),
    INSTALLING(R.drawable.host_installing),
    INSTALL_FAILED(R.drawable.down),
    MAINTENANCE(R.drawable.maintenance),
    NON_OPERATIONAL(R.drawable.nonoperational),
    NON_RESPONSIVE(R.drawable.down),
    PENDING_APPROVAL(R.drawable.unconfigured),
    PREPARING_FOR_MAINTENANCE(R.drawable.host_prepare_to_migrate),
    CONNECTING(R.drawable.down),
    REBOOT(R.drawable.wait),
    UNASSIGNED(R.drawable.down),
    UP(R.drawable.up),
    INSTALLING_OS(R.drawable.unconfigured),
    KDUMPING(R.drawable.wait);

    HostStatus(int resource) {
        this.resource = resource;
    }

    private final int resource;

    public int getResource() {
        return resource;
    }

    public static HostStatus fromString(String status) {
        try {
            return HostStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            // this is the error status also on engine
            return HostStatus.UNASSIGNED;
        }
    }
}
