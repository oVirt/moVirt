package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.Host;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suomiy on 5/18/16.
 */
public enum HostStatusMap {
    // dashboard position depends on the order
    WARNING(Host.Status.UNASSIGNED, Host.Status.MAINTENANCE, Host.Status.INSTALLING, Host.Status.REBOOT,
            Host.Status.PREPARING_FOR_MAINTENANCE, Host.Status.PENDING_APPROVAL, Host.Status.CONNECTING,
            Host.Status.INSTALLING_OS, Host.Status.KDUMPING),
    UP(Host.Status.UP),
    DOWN(Host.Status.DOWN, Host.Status.NON_RESPONSIVE, Host.Status.ERROR, Host.Status.INSTALL_FAILED,
            Host.Status.NON_OPERATIONAL, Host.Status.INITIALIZING);

    private List<Host.Status> values;
    private static Map<Host.Status, HostStatusMap> map = new EnumMap<>(Host.Status.class);

    static {
        for (HostStatusMap item : HostStatusMap.values()) {
            for (Host.Status status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    HostStatusMap(Host.Status... values) {
        this.values = Arrays.asList(values);
    }

    public List<Host.Status> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(Host.Status status) {
        HostStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
