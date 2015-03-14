package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Vm> {

    private static final String CPU_PERCENTAGE_STAT = "cpu.current.total";
    private static final String TOTAL_MEMORY_STAT = "memory.installed";
    private static final String USED_MEMORY_STAT = "memory.used";

    // public for json mapping
    public String id;
    public String name;
    public Status status;
    public Cluster cluster;
    public Statistics statistics;
    public String memory;
    public Display display;
    public Os os;
    public Cpu cpu;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Display {
        public String address, port, type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Os {
        public String type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cpu {
        public Topology topology;
    }

    @Override
    public String toString() {
        return String.format("Vm: name=%s, id=%s, status=%s, clusterId=%s",
                             name, id, status.state, cluster.id);
    }

    public org.ovirt.mobile.movirt.model.Vm toEntity() {
        org.ovirt.mobile.movirt.model.Vm vm = new org.ovirt.mobile.movirt.model.Vm();
        vm.setId(id);
        vm.setName(name);
        vm.setStatus(mapStatus(status.state));
        vm.setClusterId(cluster.id);

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
        }

        try {
            vm.setMemorySizeMb(Long.parseLong(memory) / (1024 * 1024));
        } catch (Exception e) {
            vm.setMemorySizeMb(-1);
        }

        vm.setSockets(Integer.parseInt(cpu.topology.sockets));
        vm.setCoresPerSocket(Integer.parseInt(cpu.topology.cores));

        vm.setOsType(os.type);

        vm.setDisplayType(mapDisplay(display.type));
        vm.setDisplayAddress(display.address);
        try {
            vm.setDisplayPort(Integer.parseInt(display.port));
        } catch (Exception e) {
            vm.setDisplayPort(-1);
        }

        return vm;
    }

    private static org.ovirt.mobile.movirt.model.Vm.Status mapStatus(String status) {
        return org.ovirt.mobile.movirt.model.Vm.Status.valueOf(status.toUpperCase());
    }

    private static org.ovirt.mobile.movirt.model.Vm.Display mapDisplay(String display) {
        return org.ovirt.mobile.movirt.model.Vm.Display.valueOf(display.toUpperCase());
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
