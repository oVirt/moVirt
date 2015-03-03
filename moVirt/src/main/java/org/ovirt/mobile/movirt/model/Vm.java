package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

import java.util.Arrays;
import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.TABLE;

@DatabaseTable(tableName = TABLE)
public class Vm extends OVirtEntity implements OVirtContract.Vm {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Status {
        UNASSIGNED(R.drawable.vm_question_mark),
        DOWN(R.drawable.vm_down),
        UP(R.drawable.vm_running),
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
        POWERING_DOWN(R.drawable.vm_powering_down);

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
        SHUTDOWN(Status.UP),
        POWEROFF(Status.UP, Status.POWERING_UP, Status.POWERING_DOWN),
        REBOOT(Status.UP);

        private final List<Status> validStates;

        public List<Status> getValidStates() {
            return validStates;
        }

        Command(Status ...validStates) {
            this.validStates = Arrays.asList(validStates);
        }

        public boolean canExecute(Status status) {
            return validStates.contains(status);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vm)) return false;
        if (!super.equals(o)) return false;

        Vm vm = (Vm) o;

        if (Double.compare(vm.cpuUsage, cpuUsage) != 0) return false;
        if (Double.compare(vm.memoryUsage, memoryUsage) != 0) return false;
        if (!clusterId.equals(vm.clusterId)) return false;
        if (status != vm.status) return false;

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
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(STATUS, getStatus().toString());
        contentValues.put(CLUSTER_ID, getClusterId());
        contentValues.put(CPU_USAGE, getCpuUsage());
        contentValues.put(MEMORY_USAGE, getMemoryUsage());
        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setStatus(cursorHelper.getEnum(STATUS, Vm.Status.class));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
        setCpuUsage(cursorHelper.getDouble(CPU_USAGE));
        setMemoryUsage(cursorHelper.getDouble(MEMORY_USAGE));
    }
}
