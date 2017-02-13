package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtNamedEntity;
import org.ovirt.mobile.movirt.model.enums.HostStatus;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Host.TABLE;

@DatabaseTable(tableName = TABLE)
public class Host extends OVirtNamedEntity implements OVirtContract.Host {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = STATUS, canBeNull = false)
    private HostStatus status;

    @DatabaseField(columnName = CLUSTER_ID, canBeNull = false)
    private String clusterId;

    @DatabaseField(columnName = CPU_USAGE)
    private double cpuUsage;

    @DatabaseField(columnName = MEMORY_USAGE)
    private double memoryUsage;

    @DatabaseField(columnName = MEMORY_SIZE)
    private long memorySize;

    @DatabaseField(columnName = USED_MEMORY_SIZE)
    private long usedMemorySize;

    @DatabaseField(columnName = SOCKETS)
    private int sockets;

    @DatabaseField(columnName = CORES_PER_SOCKET)
    private int coresPerSocket;

    @DatabaseField(columnName = THREADS_PER_CORE)
    private int threadsPerCore;

    @DatabaseField(columnName = OS_VERSION)
    private String osVersion;

    @DatabaseField(columnName = ADDRESS)
    private String address;

    @DatabaseField(columnName = ACTIVE)
    private int active;

    @DatabaseField(columnName = MIGRATING)
    private int migrating;

    @DatabaseField(columnName = TOTAL)
    private int total;

    @DatabaseField(columnName = CPU_SPEED)
    private long cpuSpeed;

    public HostStatus getStatus() {
        return status;
    }

    public void setStatus(HostStatus status) {
        this.status = status;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    @Override
    public long getUsedMemorySize() {
        return usedMemorySize;
    }

    @Override
    public void setUsedMemorySize(long usedMemorySize) {
        this.usedMemorySize = usedMemorySize;
    }

    public int getSockets() {
        return sockets;
    }

    public void setSockets(int sockets) {
        this.sockets = sockets;
    }

    public int getCoresPerSocket() {
        return coresPerSocket;
    }

    public void setCoresPerSocket(int coresPerSocket) {
        this.coresPerSocket = coresPerSocket;
    }

    public int getThreadsPerCore() {
        return threadsPerCore;
    }

    public void setThreadsPerCore(int threadsPerCore) {
        this.threadsPerCore = threadsPerCore;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getMigrating() {
        return migrating;
    }

    public void setMigrating(int migrating) {
        this.migrating = migrating;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public long getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Host host = (Host) o;

        if (!ObjectUtils.equals(clusterId, host.clusterId)) return false;
        if (status != host.status) return false;
        if (Double.compare(host.cpuUsage, cpuUsage) != 0) return false;
        if (Double.compare(host.memoryUsage, memoryUsage) != 0) return false;
        if (memorySize != host.memorySize) return false;
        if (usedMemorySize != host.usedMemorySize) return false;
        if (sockets != host.sockets) return false;
        if (coresPerSocket != host.coresPerSocket) return false;
        if (threadsPerCore != host.threadsPerCore) return false;
        if (!ObjectUtils.equals(osVersion, host.osVersion)) return false;
        if (!ObjectUtils.equals(address, host.address)) return false;
        if (active != host.active) return false;
        if (migrating != host.migrating) return false;
        if (total != host.total) return false;
        if (cpuSpeed != host.cpuSpeed) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + status.hashCode();
        result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);

        temp = Double.doubleToLongBits(cpuUsage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(memoryUsage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(usedMemorySize);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (memorySize ^ (memorySize >>> 32));
        result = 31 * result + sockets;
        result = 31 * result + coresPerSocket;
        result = 31 * result + threadsPerCore;
        result = 31 * result + (osVersion != null ? osVersion.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + active;
        result = 31 * result + migrating;
        result = 31 * result + total;
        result = 31 * result + (int) (cpuSpeed ^ (cpuSpeed >>> 32));

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(STATUS, getStatus().toString());
        values.put(CLUSTER_ID, getClusterId());
        values.put(CPU_USAGE, getCpuUsage());
        values.put(MEMORY_USAGE, getMemoryUsage());
        values.put(MEMORY_SIZE, getMemorySize());
        values.put(USED_MEMORY_SIZE, getUsedMemorySize());
        values.put(SOCKETS, getSockets());
        values.put(CORES_PER_SOCKET, getCoresPerSocket());
        values.put(THREADS_PER_CORE, getThreadsPerCore());
        values.put(OS_VERSION, getOsVersion());
        values.put(ADDRESS, getAddress());
        values.put(ACTIVE, getActive());
        values.put(MIGRATING, getMigrating());
        values.put(TOTAL, getTotal());
        values.put(CPU_SPEED, getCpuSpeed());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setStatus(cursorHelper.getEnum(STATUS, HostStatus.class));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
        setCpuUsage(cursorHelper.getDouble(CPU_USAGE));
        setMemoryUsage(cursorHelper.getDouble(MEMORY_USAGE));
        setMemorySize(cursorHelper.getLong(MEMORY_SIZE));
        setUsedMemorySize(cursorHelper.getLong(USED_MEMORY_SIZE));
        setSockets(cursorHelper.getInt(SOCKETS));
        setCoresPerSocket(cursorHelper.getInt(CORES_PER_SOCKET));
        setThreadsPerCore(cursorHelper.getInt(THREADS_PER_CORE));
        setOsVersion(cursorHelper.getString(OS_VERSION));
        setAddress(cursorHelper.getString(ADDRESS));
        setActive(cursorHelper.getInt(ACTIVE));
        setMigrating(cursorHelper.getInt(MIGRATING));
        setTotal(cursorHelper.getInt(TOTAL));
        setCpuSpeed(cursorHelper.getLong(CPU_SPEED));
    }
}
