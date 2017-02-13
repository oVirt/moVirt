package org.ovirt.mobile.movirt.ui.dashboard.maps;

import java.util.HashMap;
import java.util.Map;

public enum DashboardPosition {
    UNKNOWN(-1), FIRST(0), SECOND(1), THIRD(2);

    private static Map<Integer, DashboardPosition> map;
    private final int value;

    static {
        DashboardPosition[] values = DashboardPosition.values();
        map = new HashMap<>(values.length);

        for (DashboardPosition dp : values) {
            map.put(dp.getValue(), dp);
        }
    }

    DashboardPosition(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DashboardPosition fromValue(int value) {
        return map.get(value);
    }
}
