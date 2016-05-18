package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.StorageDomain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suomiy on 5/18/16.
 */
public enum StorageStatusMap {
    // dashboard position depends on the order
    WARNING(StorageDomain.Status.UNATTACHED, StorageDomain.Status.INACTIVE, StorageDomain.Status.MAINTENANCE,
            StorageDomain.Status.PREPARING_FOR_MAINTENANCE, StorageDomain.Status.MIXED,
            StorageDomain.Status.DETACHING, StorageDomain.Status.ACTIVATING),
    UP(StorageDomain.Status.ACTIVE),
    DOWN(StorageDomain.Status.UNKNOWN, StorageDomain.Status.LOCKED);

    private List<StorageDomain.Status> values;
    private static Map<StorageDomain.Status, StorageStatusMap> map = new HashMap<>();

    static {
        for (StorageStatusMap item : StorageStatusMap.values()) {
            for (StorageDomain.Status status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    StorageStatusMap(StorageDomain.Status... values) {
        this.values = Arrays.asList(values);
    }

    public List<StorageDomain.Status> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(StorageDomain.Status status) {
        StorageStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
