package org.ovirt.mobile.movirt.model;

import com.j256.ormlite.table.DatabaseTable;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Cluster.TABLE;

@DatabaseTable(tableName = TABLE)
public class Cluster extends OVirtEntity {

}
