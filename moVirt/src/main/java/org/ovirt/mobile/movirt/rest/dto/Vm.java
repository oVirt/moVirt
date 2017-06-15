package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.rest.dto.common.Statistic;
import org.ovirt.mobile.movirt.rest.dto.common.Statistics;
import org.ovirt.mobile.movirt.rest.dto.common.VmCpu;
import org.ovirt.mobile.movirt.rest.dto.common.VmOs;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Vm>, HasId {

    private static final String CPU_PERCENTAGE_STAT = "cpu.current.total";
    private static final String TOTAL_MEMORY_STAT = "memory.installed";
    private static final String USED_MEMORY_STAT = "memory.used";

    // public for json mapping
    public String id;
    public String name;
    public Statistics statistics;
    public String memory;
    public VmOs os;
    public VmCpu cpu;

    @Override
    public String getId() {
        return id;
    }

    public org.ovirt.mobile.movirt.model.Vm toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.Vm vm = new org.ovirt.mobile.movirt.model.Vm();
        vm.setIds(accountId, id);
        vm.setName(name);

        if (statistics != null && statistics.statistic != null) {
            BigDecimal cpu = getStatisticValueByName(CPU_PERCENTAGE_STAT, statistics.statistic);
            BigDecimal totalMemory = getStatisticValueByName(TOTAL_MEMORY_STAT, statistics.statistic);
            BigDecimal usedMemory = getStatisticValueByName(USED_MEMORY_STAT, statistics.statistic);

            vm.setCpuUsage(cpu.doubleValue());
            if (BigDecimal.ZERO.equals(totalMemory)) {
                vm.setMemoryUsage(0);
            } else {
                vm.setMemoryUsage(100 * usedMemory.divide(totalMemory, 3, RoundingMode.HALF_UP).doubleValue());
            }

            vm.setUsedMemorySize(usedMemory.longValue());
        }

        vm.setMemorySize(ObjectUtils.parseLong(memory));
        vm.setSockets(VmCpu.socketsOrDefault(cpu));
        vm.setCoresPerSocket(VmCpu.coresPerSocketOrDefault(cpu));

        if (os != null) {
            vm.setOsType(os.type);
        }

        return vm;
    }

    private static BigDecimal getStatisticValueByName(String name, List<Statistic> statistics) {
        for (Statistic statistic : statistics) {
            if (name.equals(statistic.name)) {
                return new BigDecimal(statistic.values.value.get(0).datum);
            }
        }
        return BigDecimal.ZERO;
    }
}
