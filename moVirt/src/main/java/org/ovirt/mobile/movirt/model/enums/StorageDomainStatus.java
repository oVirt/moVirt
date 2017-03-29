package org.ovirt.mobile.movirt.model.enums;

import org.ovirt.mobile.movirt.R;

public enum StorageDomainStatus {
    ACTIVE(R.drawable.up),
    INACTIVE(R.drawable.down),
    LOCKED(R.drawable.lock),
    MIXED(R.drawable.unconfigured),
    UNATTACHED(R.drawable.torn_chain),
    MAINTENANCE(R.drawable.maintenance),
    PREPARING_FOR_MAINTENANCE(R.drawable.lock),
    DETACHING(R.drawable.lock),
    ACTIVATING(R.drawable.lock),
    UNKNOWN(R.drawable.down);

    StorageDomainStatus(int resource) {
        this.resource = resource;
    }

    private final int resource;

    public int getResource() {
        return resource;
    }

    public static StorageDomainStatus fromString(String status) {
        try {
            if (status == null) { // sometimes behaves as unknown when null
                return StorageDomainStatus.ACTIVE;
            }

            return StorageDomainStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return StorageDomainStatus.UNKNOWN;
        }
    }
}
