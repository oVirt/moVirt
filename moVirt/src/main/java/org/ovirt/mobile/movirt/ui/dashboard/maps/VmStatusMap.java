package org.ovirt.mobile.movirt.ui.dashboard.maps;

import org.ovirt.mobile.movirt.model.Vm;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suomiy on 5/18/16.
 */
public enum VmStatusMap {
    // dashboard position depends on the order
    WARNING(Vm.Status.POWERING_UP, Vm.Status.PAUSED, Vm.Status.MIGRATING, Vm.Status.UNKNOWN,
            Vm.Status.WAIT_FOR_LAUNCH, Vm.Status.SUSPENDED, Vm.Status.POWERING_DOWN, Vm.Status.UNASSIGNED),
    UP(Vm.Status.UP, Vm.Status.SAVING_STATE, Vm.Status.RESTORING_STATE),
    DOWN(Vm.Status.DOWN, Vm.Status.NOT_RESPONDING, Vm.Status.REBOOT_IN_PROGRESS,
            Vm.Status.IMAGE_LOCKED);

    private final List<Vm.Status> values;
    private static Map<Vm.Status, VmStatusMap> map = new EnumMap<>(Vm.Status.class);

    static {
        for (VmStatusMap item : VmStatusMap.values()) {
            for (Vm.Status status : item.getValues()) {
                map.put(status, item);
            }
        }
    }

    VmStatusMap(Vm.Status... values) {
        this.values = Collections.unmodifiableList(Arrays.asList(values));
    }

    public List<Vm.Status> getValues() {
        return values;
    }

    public static DashboardPosition getDashboardPosition(Vm.Status status) {
        VmStatusMap result = map.get(status);
        return result == null ? DashboardPosition.UNKNOWN : DashboardPosition.fromValue(result.ordinal());
    }
}
