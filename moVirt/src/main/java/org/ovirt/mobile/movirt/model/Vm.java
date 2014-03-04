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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vm)) return false;
        if (!super.equals(o)) return false;

        Vm vm = (Vm) o;

        if (!ObjectUtils.equals(clusterId, vm.clusterId)) return false;
        if (!ObjectUtils.equals(status, vm.status)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + clusterId.hashCode();
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
