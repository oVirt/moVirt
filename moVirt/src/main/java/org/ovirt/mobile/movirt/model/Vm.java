package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.*;

@DatabaseTable(tableName = TABLE)
public class Vm extends OVirtEntity {

    public static enum Status {
        UP,
        DOWN,
        UNKNOWN
    }

 //   @DatabaseField(columnName = STATUS, dataType = DataType.ENUM_INTEGER, canBeNull = false)
 //   private Status status = Status.DOWN;
    @DatabaseField(columnName = STATUS, canBeNull = false)
    private Status status;

    @DatabaseField(columnName = CLUSTER_ID, canBeNull = false)
    private String clusterId;

    @DatabaseField(columnName = CPU_USAGE)
    private int cpuUsage;

    @DatabaseField(columnName = MEMORY_USAGE)
    private int memoryUsage;

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

    public int getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(int cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public int getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(int memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vm)) return false;
        if (!super.equals(o)) return false;

        Vm vm = (Vm) o;

        if (cpuUsage != vm.cpuUsage) return false;
        if (memoryUsage != vm.memoryUsage) return false;
        if (clusterId != null ? !clusterId.equals(vm.clusterId) : vm.clusterId != null)
            return false;
        if (status != vm.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
        result = 31 * result + cpuUsage;
        result = 31 * result + memoryUsage;
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(STATUS, getStatus().toString());
        contentValues.put(CLUSTER_ID, getClusterId());
        return contentValues;
    }
}
