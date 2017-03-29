package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.enums.StorageDomainStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum StorageStatusMap {
    // dashboard position depends on the order
    WARNING(StorageDomainStatus.UNATTACHED, StorageDomainStatus.INACTIVE, StorageDomainStatus.MAINTENANCE,
            StorageDomainStatus.PREPARING_FOR_MAINTENANCE, StorageDomainStatus.MIXED,
            StorageDomainStatus.DETACHING, StorageDomainStatus.ACTIVATING),
    UP(StorageDomainStatus.ACTIVE),
    DOWN(StorageDomainStatus.UNKNOWN, StorageDomainStatus.LOCKED);

    private final List<StorageDomainStatus> values;
    private static Map<StorageDomainStatus, StorageStatusMap> map = new EnumMap<>(StorageDomainStatus.class);

    static {
        for (StorageStatusMap item : StorageStatusMap.values()) {
            for (StorageDomainStatus status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    StorageStatusMap(StorageDomainStatus... values) {
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    public List<StorageDomainStatus> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(StorageDomainStatus status) {
        StorageStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
