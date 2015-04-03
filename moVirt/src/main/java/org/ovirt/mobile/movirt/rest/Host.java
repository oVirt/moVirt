package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class Host implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Host> {

    private static final String IDLE_CPU_PERCENTAGE_STAT = "cpu.current.idle";
    private static final String TOTAL_MEMORY_STAT = "memory.total";
    private static final String USED_MEMORY_STAT = "memory.used";

    // public for json mapping
    public String id;
    public String name;
    public Status status;
    public Cluster cluster;
    public Statistics statistics;
    public String memory;
    public Os os;
    public Cpu cpu;
    public Summary summary;
    public String address;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version {
        public String major;
        public String full_version;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Os {
        public String type;
        public Version version;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cpu {
        public Topology topology;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        public String active;
        public String migrating;
        public String total;
    }

    @Override
    public org.ovirt.mobile.movirt.model.Host toEntity() {
        org.ovirt.mobile.movirt.model.Host host = new org.ovirt.mobile.movirt.model.Host();
        host.setId(id);
        host.setName(name);
        host.setStatus(mapStatus(status.state));
        host.setClusterId(cluster.id);

        if (statistics != null && statistics.statistic != null) {
            BigDecimal cpu = new BigDecimal(100).subtract(getStatisticValueByName(IDLE_CPU_PERCENTAGE_STAT, statistics.statistic));
            BigDecimal totalMemory = getStatisticValueByName(TOTAL_MEMORY_STAT, statistics.statistic);
            BigDecimal usedMemory = getStatisticValueByName(USED_MEMORY_STAT, statistics.statistic);

            host.setCpuUsage(cpu.doubleValue());
            if (BigDecimal.ZERO.equals(totalMemory)) {
                host.setMemoryUsage(0);
            } else {
                host.setMemoryUsage(100 * usedMemory.divide(totalMemory, 3, RoundingMode.HALF_UP).doubleValue());
            }
        }

        try {
            host.setMemorySizeMb(Long.parseLong(memory) / (1024 * 1024));
        } catch (Exception e) {
            host.setMemorySizeMb(-1);
        }

        host.setSockets(Integer.parseInt(cpu.topology.sockets));
        host.setCoresPerSocket(Integer.parseInt(cpu.topology.cores));
        host.setThreadsPerCore(Integer.parseInt(cpu.topology.threads));

        host.setOsVersion(os.type + "-" + os.version.full_version);

        host.setAddress(address);

        host.setActive(Integer.parseInt(summary.active));
        host.setMigrating(Integer.parseInt(summary.migrating));
        host.setTotal(Integer.parseInt(summary.total));

        return host;
    }

    private static org.ovirt.mobile.movirt.model.Host.Status mapStatus(String state) {
        return org.ovirt.mobile.movirt.model.Host.Status.valueOf(state.toUpperCase());
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
