package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtAccountNamedEntity;
import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.TABLE;

@DatabaseTable(tableName = TABLE)
public class Vm extends OVirtAccountNamedEntity implements OVirtContract.Vm {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = STATUS, canBeNull = false)
    private VmStatus status;

    @DatabaseField(columnName = HOST_ID)
    private String hostId;

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

    @DatabaseField(columnName = OS_TYPE)
    private String osType;

    private transient List<Nic> nics;

    public VmStatus getStatus() {
        return status;
    }

    public void setStatus(VmStatus status) {
        this.status = status;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public double getCpuUsage() {
        return cpuUsage;
    }

    @Override
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    @Override
    public double getMemoryUsage() {
        return memoryUsage;
    }

    @Override
    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    @Override
    public long getMemorySize() {
        return memorySize;
    }

    @Override
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

    @Override
    public int getSockets() {
        return sockets;
    }

    @Override
    public void setSockets(int sockets) {
        this.sockets = sockets;
    }

    @Override
    public int getCoresPerSocket() {
        return coresPerSocket;
    }

    @Override
    public void setCoresPerSocket(int coresPerSocket) {
        this.coresPerSocket = coresPerSocket;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public List<Nic> getNics() {
        return nics;
    }

    public void setNics(List<Nic> nics) {
        this.nics = nics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Vm vm = (Vm) o;

        if (coresPerSocket != vm.coresPerSocket) return false;
        if (Double.compare(vm.cpuUsage, cpuUsage) != 0) return false;
        if (memorySize != vm.memorySize) return false;
        if (usedMemorySize != vm.usedMemorySize) return false;
        if (Double.compare(vm.memoryUsage, memoryUsage) != 0) return false;
        if (sockets != vm.sockets) return false;
        if (!ObjectUtils.equals(hostId, vm.hostId)) return false;
        if (!ObjectUtils.equals(clusterId, vm.clusterId)) return false;
        if (!ObjectUtils.equals(osType, vm.osType)) return false;
        if (status != vm.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (hostId != null ? hostId.hashCode() : 0);
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
        result = 31 * result + (osType != null ? osType.hashCode() : 0);
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(STATUS, getStatus().toString());
        contentValues.put(HOST_ID, getHostId());
        contentValues.put(CLUSTER_ID, getClusterId());
        contentValues.put(CPU_USAGE, getCpuUsage());
        contentValues.put(MEMORY_USAGE, getMemoryUsage());
        contentValues.put(MEMORY_SIZE, getMemorySize());
        contentValues.put(USED_MEMORY_SIZE, getUsedMemorySize());
        contentValues.put(SOCKETS, getSockets());
        contentValues.put(CORES_PER_SOCKET, getCoresPerSocket());
        contentValues.put(OS_TYPE, getOsType());
        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setStatus(cursorHelper.getEnum(STATUS, VmStatus.class));
        setHostId(cursorHelper.getString(HOST_ID));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
        setCpuUsage(cursorHelper.getDouble(CPU_USAGE));
        setMemoryUsage(cursorHelper.getDouble(MEMORY_USAGE));
        setMemorySize(cursorHelper.getLong(MEMORY_SIZE));
        setUsedMemorySize(cursorHelper.getLong(USED_MEMORY_SIZE));
        setSockets(cursorHelper.getInt(SOCKETS));
        setCoresPerSocket(cursorHelper.getInt(CORES_PER_SOCKET));
        setOsType(cursorHelper.getString(OS_TYPE));
    }
}
