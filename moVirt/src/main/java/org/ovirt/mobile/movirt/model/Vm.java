package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.SnapshotEmbeddableEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.TABLE;

@DatabaseTable(tableName = TABLE)
public class Vm extends SnapshotEmbeddableEntity implements OVirtContract.Vm {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Status {
        UNASSIGNED(R.drawable.vm_question_mark),
        DOWN(R.drawable.down),
        UP(R.drawable.up),
        POWERING_UP(R.drawable.vm_powering_up),
        PAUSED(R.drawable.vm_paused),
        MIGRATING(R.drawable.vm_migrating),
        UNKNOWN(R.drawable.vm_question_mark),
        NOT_RESPONDING(R.drawable.vm_question_mark),
        WAIT_FOR_LAUNCH(R.drawable.vm_wait_for_launch),
        REBOOT_IN_PROGRESS(R.drawable.vm_reboot_in_progress),
        SAVING_STATE(R.drawable.vm_wait),
        SUSPENDED(R.drawable.vm_suspened),
        IMAGE_LOCKED(R.drawable.vm_wait),
        POWERING_DOWN(R.drawable.vm_powering_down),
        RESTORING_STATE(R.drawable.vm_powering_up);

        Status(int resource) {
            this.resource = resource;
        }

        private final int resource;

        public int getResource() {
            return resource;
        }

    }

    public enum Command {
        RUN(Status.DOWN, Status.PAUSED),
        STOP(Status.WAIT_FOR_LAUNCH, Status.UP, Status.POWERING_DOWN, Status.POWERING_UP,
                Status.REBOOT_IN_PROGRESS, Status.MIGRATING, Status.SUSPENDED, Status.PAUSED,
                Status.NOT_RESPONDING),
        REBOOT(Status.UP, Status.POWERING_UP),
        START_MIGRATION(Status.UNASSIGNED, Status.UP, Status.POWERING_UP,
                Status.UNKNOWN, Status.WAIT_FOR_LAUNCH, Status.REBOOT_IN_PROGRESS,
                Status.SAVING_STATE, Status.SUSPENDED, Status.IMAGE_LOCKED, Status.POWERING_DOWN),
        CANCEL_MIGRATION(Status.MIGRATING),
        CONSOLE(Status.UP, Status.POWERING_UP, Status.REBOOT_IN_PROGRESS, Status.POWERING_DOWN,
                Status.PAUSED),
        // used in snapshots
        SAVE_MEMORY(Status.UNASSIGNED, Status.UP, Status.POWERING_UP, Status.PAUSED,
                Status.MIGRATING, Status.UNKNOWN, Status.NOT_RESPONDING, Status.REBOOT_IN_PROGRESS,
                Status.SAVING_STATE, Status.SUSPENDED, Status.IMAGE_LOCKED, Status.POWERING_DOWN),
        NOT_RUNNING(Status.DOWN);

        private final List<Status> validStates;

        public List<Status> getValidStates() {
            return validStates;
        }

        Command(Status... validStates) {
            this.validStates = Arrays.asList(validStates);
        }

        public boolean canExecute(Status status) {
            return validStates.contains(status);
        }
    }

    @DatabaseField(columnName = STATUS, canBeNull = false)
    private Status status;

    @DatabaseField(columnName = HOST_ID, canBeNull = false)
    private String hostId;

    @DatabaseField(columnName = CLUSTER_ID, canBeNull = false)
    private String clusterId;

    @DatabaseField(columnName = CPU_USAGE)
    private double cpuUsage;

    @DatabaseField(columnName = MEMORY_USAGE)
    private double memoryUsage;

    @DatabaseField(columnName = MEMORY_SIZE)
    private long memorySize;

    @DatabaseField(columnName = SOCKETS)
    private int sockets;

    @DatabaseField(columnName = CORES_PER_SOCKET)
    private int coresPerSocket;

    @DatabaseField(columnName = OS_TYPE)
    private String osType;

    private transient List<Nic> nics;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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
        contentValues.put(SOCKETS, getSockets());
        contentValues.put(CORES_PER_SOCKET, getCoresPerSocket());
        contentValues.put(OS_TYPE, getOsType());
        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setStatus(cursorHelper.getEnum(STATUS, Vm.Status.class));
        setHostId(cursorHelper.getString(HOST_ID));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
        setCpuUsage(cursorHelper.getDouble(CPU_USAGE));
        setMemoryUsage(cursorHelper.getDouble(MEMORY_USAGE));
        setMemorySize(cursorHelper.getLong(MEMORY_SIZE));
        setSockets(cursorHelper.getInt(SOCKETS));
        setCoresPerSocket(cursorHelper.getInt(CORES_PER_SOCKET));
        setOsType(cursorHelper.getString(OS_TYPE));
    }
}
