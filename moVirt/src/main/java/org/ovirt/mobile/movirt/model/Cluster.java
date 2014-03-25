package org.ovirt.mobile.movirt.model;

import android.net.Uri;

import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Cluster.TABLE;

@DatabaseTable(tableName = TABLE)
public class Cluster extends OVirtEntity implements OVirtContract.Cluster {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }
}
