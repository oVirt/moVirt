package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class Host implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Host> {

    private static final String USER_CPU_PERCENTAGE_STAT = "cpu.current.user";
    private static final String SYSTEM_CPU_PERCENTAGE_STAT = "cpu.current.system";
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
    public static class Os {
        public String type;
        public Version version;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cpu {
        public Topology topology;
        public String speed;
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
        host.setStatus(mapStatus(status));
        if (cluster != null) {
            host.setClusterId(cluster.id);
        }

        if (statistics != null && statistics.statistic != null) {
            BigDecimal cpu = getStatisticValueByName(USER_CPU_PERCENTAGE_STAT, statistics.statistic)
                    .add(getStatisticValueByName(SYSTEM_CPU_PERCENTAGE_STAT, statistics.statistic));
            BigDecimal totalMemory = getStatisticValueByName(TOTAL_MEMORY_STAT, statistics.statistic);
            BigDecimal usedMemory = getStatisticValueByName(USED_MEMORY_STAT, statistics.statistic);

            host.setCpuUsage(cpu.doubleValue());
            if (BigDecimal.ZERO.equals(totalMemory)) {
                host.setMemoryUsage(0);
            } else {
                host.setMemoryUsage(100 * usedMemory.divide(totalMemory, 3, RoundingMode.HALF_UP).doubleValue());
            }
        }

        host.setMemorySize(ObjectUtils.parseLong(memory));

        if (cpu != null && cpu.topology != null) {
            host.setSockets(ParseUtils.intOrDefault(cpu.topology.sockets));
            host.setCoresPerSocket(ParseUtils.intOrDefault(cpu.topology.cores));
            host.setThreadsPerCore(ParseUtils.intOrDefault(cpu.topology.threads));
        } else {
            host.setSockets(-1);
            host.setCoresPerSocket(-1);
            host.setThreadsPerCore(-1);
        }


        host.setCpuSpeed(ObjectUtils.parseLong(cpu.speed));
        String osString = "";
        if (os != null && os.type != null) {
            osString = os.type;
        }
        if (os != null && os.version != null && os.version.full_version != null) {
            osString += "-" + os.version.full_version;
        }
        host.setOsVersion(osString);

        host.setAddress(address);

        if (summary != null) {
            host.setActive(ParseUtils.intOrDefault(summary.active));
            host.setMigrating(ParseUtils.intOrDefault(summary.migrating));
            host.setTotal(ParseUtils.intOrDefault(summary.total));
        }

        return host;
    }

    private static org.ovirt.mobile.movirt.model.Host.Status mapStatus(Status state) {
        try {
            return org.ovirt.mobile.movirt.model.Host.Status.valueOf(state.state.toUpperCase());
        } catch (Exception e) {
            // this is the error status also on engine
            return org.ovirt.mobile.movirt.model.Host.Status.UNASSIGNED;
        }

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
