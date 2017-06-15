package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.enums.HostStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum HostStatusMap {
    // dashboard position depends on the order
    WARNING(HostStatus.UNASSIGNED, HostStatus.MAINTENANCE, HostStatus.INSTALLING, HostStatus.REBOOT,
            HostStatus.PREPARING_FOR_MAINTENANCE, HostStatus.PENDING_APPROVAL, HostStatus.CONNECTING,
            HostStatus.INSTALLING_OS, HostStatus.KDUMPING),
    UP(HostStatus.UP),
    DOWN(HostStatus.DOWN, HostStatus.NON_RESPONSIVE, HostStatus.ERROR, HostStatus.INSTALL_FAILED,
            HostStatus.NON_OPERATIONAL, HostStatus.INITIALIZING);

    private final List<HostStatus> values;
    private static Map<HostStatus, HostStatusMap> map = new EnumMap<>(HostStatus.class);

    static {
        for (HostStatusMap item : HostStatusMap.values()) {
            for (HostStatus status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    HostStatusMap(HostStatus... values) {
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    public List<HostStatus> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(HostStatus status) {
        HostStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
