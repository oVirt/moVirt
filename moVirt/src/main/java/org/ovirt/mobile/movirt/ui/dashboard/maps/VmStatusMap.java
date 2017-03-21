package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.enums.VmStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum VmStatusMap {
    // dashboard position depends on the order
    WARNING(VmStatus.POWERING_UP, VmStatus.PAUSED, VmStatus.MIGRATING, VmStatus.UNKNOWN,
            VmStatus.WAIT_FOR_LAUNCH, VmStatus.SUSPENDED, VmStatus.POWERING_DOWN, VmStatus.UNASSIGNED),
    UP(VmStatus.UP, VmStatus.SAVING_STATE, VmStatus.RESTORING_STATE),
    DOWN(VmStatus.DOWN, VmStatus.NOT_RESPONDING, VmStatus.REBOOT_IN_PROGRESS,
            VmStatus.IMAGE_LOCKED);

    private final List<VmStatus> values;
    private static Map<VmStatus, VmStatusMap> map = new EnumMap<>(VmStatus.class);

    static {
        for (VmStatusMap item : VmStatusMap.values()) {
            for (VmStatus status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    VmStatusMap(VmStatus... values) {
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    public List<VmStatus> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(VmStatus status) {
        VmStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
