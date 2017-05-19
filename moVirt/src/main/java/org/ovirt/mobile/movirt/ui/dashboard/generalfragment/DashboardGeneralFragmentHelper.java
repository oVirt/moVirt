package org.ovirt.mobile.movirt.ui.dashboard.generalfragment;

import android.support.v4.util.Pair;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.util.usage.Cores;
import org.ovirt.mobile.movirt.util.usage.MemorySize;
import org.ovirt.mobile.movirt.util.usage.Percentage;

import java.util.List;

public class DashboardGeneralFragmentHelper {

    public static <T extends OVirtContract.HasCoresPerSocket & OVirtContract.HasSockets & OVirtContract.HasCpuUsage>
    Pair<UtilizationResource, Cores> getCpuUtilization(List<T> entities) {
        Cores allCores = new Cores();
        double usedPercentagesSum = 0;

        for (T entity : entities) {
            Cores entityCores = new Cores(entity);

            usedPercentagesSum += entityCores.getValue() * entity.getCpuUsage();
            allCores.addValue(entityCores);
        }

        // average of all host usages
        Percentage used = new Percentage((long) usedPercentagesSum / (allCores.getValue() == 0 ? 1 : allCores.getValue()));
        Percentage total = new Percentage(100);
        Percentage available = new Percentage(total.getValue() - used.getValue());

        return new Pair<>(new UtilizationResource(used, total, available), allCores);
    }

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
