package org.ovirt.mobile.movirt.ui.dashboard.generalfragment;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.util.usage.MemorySize;

import java.util.List;

public class DashboardGeneralFragmentHelper {

    public static <T extends OVirtContract.HasMemory> UtilizationResource getMemoryUtilization(List<T> entities) {
        MemorySize total = new MemorySize();
        MemorySize used = new MemorySize();
        MemorySize available;

        for (T entity : entities) {
            total.addValue(entity.getMemorySize());
            used.addValue(entity.getUsedMemorySize());
        }

        available = new MemorySize(total.getValue() - used.getValue());

        return new UtilizationResource(used, total, available);
    }
}
