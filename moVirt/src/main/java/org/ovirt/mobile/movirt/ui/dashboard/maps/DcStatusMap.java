package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.DataCenter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suomiy on 5/18/16.
 */
public enum DcStatusMap {
    // dashboard position depends on the order
    WARNING(DataCenter.Status.UNINITIALIZED, DataCenter.Status.MAINTENANCE, DataCenter.Status.CONTEND),
    UP(DataCenter.Status.UP),
    DOWN(DataCenter.Status.NOT_OPERATIONAL, DataCenter.Status.PROBLEMATIC);

    private List<DataCenter.Status> values;
    private static Map<DataCenter.Status, DcStatusMap> map = new EnumMap<>(DataCenter.Status.class);

    static {
        for (DcStatusMap item : DcStatusMap.values()) {
            for (DataCenter.Status status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    DcStatusMap(DataCenter.Status... values) {
        this.values = Arrays.asList(values);
    }

    public List<DataCenter.Status> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(DataCenter.Status status) {
        DcStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
