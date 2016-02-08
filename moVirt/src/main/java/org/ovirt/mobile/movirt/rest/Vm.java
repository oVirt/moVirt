package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.util.RestHelper;

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
    public Host host;
    public Cluster cluster;
    public Statistics statistics;
    public String memory;
    public Display display;
    public Os os;
    public Cpu cpu;
    public Disks disks;
    public Nics nics;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Display {
        public String address, port, type, secure_port;
        public Certificate certificate;
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
        vm.setStatus(mapStatus(status));
        vm.setClusterId(cluster.id);
        vm.setHostId(host != null ? host.id : "");

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

        if (cpu != null && cpu.topology != null) {
            vm.setSockets(ParseUtils.intOrDefault(cpu.topology.sockets));
            vm.setCoresPerSocket(ParseUtils.intOrDefault(cpu.topology.cores));
        } else {
            vm.setSockets(-1);
            vm.setCoresPerSocket(-1);
        }

        if (os != null) {
            vm.setOsType(os.type);
        }

        if (display != null) {
            vm.setDisplayType(mapDisplay(display.type));
            vm.setDisplayAddress(display.address != null ? display.address : "");
            vm.setCertificateSubject((display.certificate != null && display.certificate.subject != null) ? display.certificate.subject : "");
        } else {
            vm.setDisplayType(org.ovirt.mobile.movirt.model.Vm.Display.VNC);
            vm.setDisplayAddress("");
        }

        try {
            vm.setDisplayPort(Integer.parseInt(display.port));
        } catch (Exception e) {
            vm.setDisplayPort(-1);
        }
        try {
            vm.setDisplaySecurePort(Integer.parseInt(display.secure_port));
        } catch (Exception e) {
            vm.setDisplaySecurePort(-1);
        }

        if (nics != null) {
            vm.setNics(RestHelper.mapToEntities(nics.nic));
        }

        if (nics != null) {
            vm.setDisks(RestHelper.mapToEntities(disks.disk));
        }

        return vm;
    }

    private static org.ovirt.mobile.movirt.model.Vm.Status mapStatus(Status status) {
        try {
            return org.ovirt.mobile.movirt.model.Vm.Status.valueOf(status.state.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.Vm.Status.UNKNOWN;
        }
    }

    private static org.ovirt.mobile.movirt.model.Vm.Display mapDisplay(String display) {
        try {
            return org.ovirt.mobile.movirt.model.Vm.Display.valueOf(display.toUpperCase());
        } catch (Exception e) {
            // not particularly nice but same behavior as on the webadmin/userportal
            return org.ovirt.mobile.movirt.model.Vm.Display.VNC;
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
