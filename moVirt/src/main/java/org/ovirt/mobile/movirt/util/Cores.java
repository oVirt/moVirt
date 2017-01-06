package org.ovirt.mobile.movirt.util;

import org.ovirt.mobile.movirt.provider.OVirtContract;

public class Cores implements UsageResource {
    private static final String unit = "";
    private long cores = 0;

    /**
     * cores initialized to 0
     */
    public Cores() {
    }

    /**
     * @param cores truncated to 0 if cores < 0
     */
    public Cores(long cores) {
        setValue(cores);
    }

    public <T extends OVirtContract.HasSockets & OVirtContract.HasCoresPerSocket> Cores(T value) {
        this(value.getCoresPerSocket(), value.getSockets());
    }

    /**
     * computed value truncated to 0 if cores < 0 || sockets < 0
     *
     * @param coresPerSocket number of cores per socket
     * @param sockets        number of sockets
     */
    public Cores(long coresPerSocket, long sockets) {
        setValue(coresPerSocket, sockets);
    }

    /**
     * @return cores
     */
    public long getValue() {
        return cores;
    }

    /**
     * added value truncated to 0 if cores < 0 || sockets < 0
     *
     * @param cores number of cores
     */
    public void setValue(long cores) {
        if (cores > 0) {
            this.cores = cores;
        }
    }

    /**
     * computed value truncated to 0 if cores < 0 || sockets < 0
     *
     * @param coresPerSocket number of cores per socket
     * @param sockets        number of sockets
     */
    public void setValue(long coresPerSocket, long sockets) {
        if (coresPerSocket > 0 && sockets > 0) {
            this.cores = sockets * coresPerSocket;
        }
    }

    /**
     * @param cores adds cores, no operation occurs if cores < 0
     */
    public void addValue(long cores) {
        if (cores > 0) {
            this.cores += cores;
        }
    }

    /**
     * adds cores, no operation occurs if cores < 0 || sockets < 0
     *
     * @param coresPerSocket number of cores per socket
     * @param sockets        number of sockets
     */
    public void addValue(long coresPerSocket, long sockets) {
        if (coresPerSocket > 0 && sockets > 0) {
            this.cores += sockets * coresPerSocket;
        }
    }

    /**
     * adds cores, no operation occurs if cores < 0 || sockets < 0
     *
     * @param value entity with socket and cores per socket
     */
    public <T extends OVirtContract.HasSockets & OVirtContract.HasCoresPerSocket> void addValue(T value) {
        addValue(value.getCoresPerSocket(), value.getSockets());
    }

    /**
     * adds cores, no operation occurs if cores < 0 || sockets < 0
     *
     * @param cores cores
     */
    public void addValue(Cores cores) {
        addValue(cores.getValue());
    }

    /**
     * @return string representation of number of cores
     */
    public String getReadableValueAsString() {
        return Long.toString(cores);
    }

    /**
     * @return ""
     */
    public String getReadableUnitAsString() {
        return unit;
    }

    @Override
    public String toString() {
        return getReadableValueAsString();
    }
}
