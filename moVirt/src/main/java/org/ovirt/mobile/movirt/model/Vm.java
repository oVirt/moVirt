package org.ovirt.mobile.movirt.model;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Vm.*;

@DatabaseTable(tableName = TABLE)
public class Vm {

    public static enum Status {
        UP,
        DOWN
    }

    public Vm() {
        id = "";
        name = "";
    }

    @DatabaseField(columnName = BaseColumns._ID, id = true)
    private String id;

    @DatabaseField(columnName = NAME, canBeNull = false)
    private String name;

    @DatabaseField(columnName = STATUS, dataType = DataType.ENUM_INTEGER, canBeNull = false)
    private Status status = Status.DOWN;

    @DatabaseField(foreign = true, columnName = CLUSTER_ID)
    private Cluster cluster;
}
