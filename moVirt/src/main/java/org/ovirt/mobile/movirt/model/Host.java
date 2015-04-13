package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Host.TABLE;

@DatabaseTable(tableName = TABLE)
public class Host extends OVirtEntity implements OVirtContract.Host {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Status {
        DOWN(R.drawable.down),
        ERROR(R.drawable.error),
        INITIALIZING(R.drawable.wait),
        INSTALLING(R.drawable.host_installing),
        INSTALL_FAILED(R.drawable.down),
        MAINTENANCE(R.drawable.host_maintenance),
        NON_OPERATIONAL(R.drawable.nonoperational),
        NON_RESPONSIVE(R.drawable.down),
        PENDING_APPROVAL(R.drawable.unconfigured),
        PREPARING_FOR_MAINTENANCE(R.drawable.host_prepare_to_migrate),
        CONNECTING(R.drawable.down),
        REBOOT(R.drawable.wait),
        UNASSIGNED(R.drawable.down),
        UP(R.drawable.up),
        INSTALLING_OS(R.drawable.unconfigured),
        KDUMPING(R.drawable.wait);

        Status(int resource) {
            this.resource = resource;
        }

        private final int resource;

        public int getResource() {
            return resource;
        }
    }

    @DatabaseField(columnName = STATUS, canBeNull = false)
    private Status status;

    @DatabaseField(columnName = CLUSTER_ID, canBeNull = false)
    private String clusterId;

    @DatabaseField(columnName = CPU_USAGE)
    private double cpuUsage;

    @DatabaseField(columnName = MEMORY_USAGE)
    private double memoryUsage;

    @DatabaseField(columnName = MEMORY_SIZE_MB)
    private long memorySizeMb;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    public long getMemorySizeMb() {
        return memorySizeMb;
    }

    public void setMemorySizeMb(long memorySizeMb) {
        this.memorySizeMb = memorySizeMb;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Host host = (Host) o;

        if (!clusterId.equals(host.clusterId)) return false;
        if (status != host.status) return false;
        if (Double.compare(host.cpuUsage, cpuUsage) != 0) return false;
        if (Double.compare(host.memoryUsage, memoryUsage) != 0) return false;
        if (memorySizeMb != host.memorySizeMb) return false;
        if (sockets != host.sockets) return false;
        if (coresPerSocket != host.coresPerSocket) return false;
        if (threadsPerCore != host.threadsPerCore) return false;
        if (!osVersion.equals(host.osVersion)) return false;
        if (!address.equals(host.address)) return false;
        if (active != host.active) return false;
        if (migrating != host.migrating) return false;
        if (total != host.total) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + status.hashCode();
        result = 31 * result + clusterId.hashCode();

        temp = Double.doubleToLongBits(cpuUsage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(memoryUsage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (memorySizeMb ^ (memorySizeMb >>> 32));
        result = 31 * result + sockets;
        result = 31 * result + coresPerSocket;
        result = 31 * result + threadsPerCore;
        result = 31 * result + osVersion.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + active;
        result = 31 * result + migrating;
        result = 31 * result + total;

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(STATUS, getStatus().toString());
        values.put(CLUSTER_ID, getClusterId());
        values.put(CPU_USAGE, getCpuUsage());
        values.put(MEMORY_USAGE, getMemoryUsage());
        values.put(MEMORY_SIZE_MB, getMemorySizeMb());
        values.put(SOCKETS, getSockets());
        values.put(CORES_PER_SOCKET, getCoresPerSocket());
        values.put(THREADS_PER_CORE, getThreadsPerCore());
        values.put(OS_VERSION, getOsVersion());
        values.put(ADDRESS, getAddress());
        values.put(ACTIVE, getActive());
        values.put(MIGRATING, getMigrating());
        values.put(TOTAL, getTotal());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setStatus(cursorHelper.getEnum(STATUS, Status.class));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
        setCpuUsage(cursorHelper.getDouble(CPU_USAGE));
        setMemoryUsage(cursorHelper.getDouble(MEMORY_USAGE));
        setMemorySizeMb(cursorHelper.getLong(MEMORY_SIZE_MB));
        setSockets(cursorHelper.getInt(SOCKETS));
        setCoresPerSocket(cursorHelper.getInt(CORES_PER_SOCKET));
        setThreadsPerCore(cursorHelper.getInt(THREADS_PER_CORE));
        setOsVersion(cursorHelper.getString(OS_VERSION));
        setAddress(cursorHelper.getString(ADDRESS));
        setActive(cursorHelper.getInt(ACTIVE));
        setMigrating(cursorHelper.getInt(MIGRATING));
        setTotal(cursorHelper.getInt(TOTAL));
    }
}
