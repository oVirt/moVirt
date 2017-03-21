package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.enums.DataCenterStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum DcStatusMap {
    // dashboard position depends on the order
    WARNING(DataCenterStatus.UNINITIALIZED, DataCenterStatus.MAINTENANCE, DataCenterStatus.CONTEND),
    UP(DataCenterStatus.UP),
    DOWN(DataCenterStatus.NOT_OPERATIONAL, DataCenterStatus.PROBLEMATIC);

    private final List<DataCenterStatus> values;
    private static Map<DataCenterStatus, DcStatusMap> map = new EnumMap<>(DataCenterStatus.class);

    static {
        for (DcStatusMap item : DcStatusMap.values()) {
            for (DataCenterStatus status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    DcStatusMap(DataCenterStatus... values) {
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    public List<DataCenterStatus> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(DataCenterStatus status) {
        DcStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
