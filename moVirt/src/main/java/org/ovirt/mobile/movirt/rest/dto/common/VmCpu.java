package org.ovirt.mobile.movirt.rest.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.rest.ParseUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VmCpu {
    public Topology topology;

    public static int socketsOrDefault(VmCpu cpu) {
        if (cpu != null && cpu.topology != null) {
            return ParseUtils.intOrDefault(cpu.topology.sockets);
        } else {
            return -1;
        }
    }

    public static int coresPerSocketOrDefault(VmCpu cpu) {
        if (cpu != null && cpu.topology != null) {
            return ParseUtils.intOrDefault(cpu.topology.cores);
        } else {
            return -1;
        }
    }
}
