package org.ovirt.mobile.movirt.model;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Cluster.*;

@DatabaseTable(tableName = TABLE)
public class Cluster {

    public Cluster() {
        id = "";
        name = "";
    }

    @DatabaseField(columnName = BaseColumns._ID, id = true)
    private String id;

    @DatabaseField(columnName = NAME, canBeNull = false)
    private String name;

    @ForeignCollectionField
    private Collection<Vm> vms;
}
