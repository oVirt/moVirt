package org.ovirt.mobile.movirt.sync.doctor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.sync.RestEntityWrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Vm implements RestEntityWrapper<org.ovirt.mobile.movirt.model.Vm> {

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("memory")
    public long memory;

    @JsonProperty("status/state")
    public String status;

    @JsonProperty("cpu/topology/sockets")
    public int sockets;

    @JsonProperty("cpu/topology/cores")
    public int cores;

    @JsonProperty("cpu/current/total")
    public int totalCpu;

    @JsonProperty("memory/installed")
    public long memoryInstalled;

    @JsonProperty("memory/used")
    public long memoryUsed;

    @JsonProperty("os/type")
    public String osType;

    @JsonProperty("display/address")
    public String displayAddress;

    @JsonProperty("display/port")
    public String displayPort;

    @JsonProperty("display/type")
    public String displayType;

    @JsonProperty("_links")
    public Links links;

    @Override
    public org.ovirt.mobile.movirt.model.Vm toEntity() {
        org.ovirt.mobile.movirt.model.Vm vm = new org.ovirt.mobile.movirt.model.Vm();
        vm.setId(id);
        vm.setName(name);
        vm.setStatus(org.ovirt.mobile.movirt.model.Vm.Status.valueOf(status.toUpperCase()));
        vm.setClusterId(links.cluster);

        BigDecimal totalMemory = BigDecimal.valueOf(memoryInstalled);
        BigDecimal usedMemory = BigDecimal.valueOf(memoryUsed);

        vm.setCpuUsage(BigDecimal.valueOf(totalCpu).doubleValue());
        if (BigDecimal.ZERO.equals(totalMemory)) {
            vm.setMemoryUsage(0);
        } else {
            vm.setMemoryUsage(100 * usedMemory.divide(totalMemory, 3, RoundingMode.HALF_UP).doubleValue());
        }


        vm.setMemorySizeMb(memory);
        vm.setSockets(sockets);
        vm.setCoresPerSocket(cores);

        vm.setOsType(osType);

        vm.setDisplayType(org.ovirt.mobile.movirt.model.Vm.Display.valueOf(displayType.toUpperCase()));
        vm.setDisplayAddress(displayAddress);
        try {
            vm.setDisplayPort(Integer.parseInt(displayPort));
        } catch (Exception e) {
            vm.setDisplayPort(-1);
        }

        return vm;
    }
}
