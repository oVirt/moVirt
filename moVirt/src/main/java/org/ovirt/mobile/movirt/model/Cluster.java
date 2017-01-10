package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtNamedEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Cluster.TABLE;

@DatabaseTable(tableName = TABLE)
public class Cluster extends OVirtNamedEntity implements OVirtContract.Cluster {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = VERSION)
    private String version;

    @DatabaseField(columnName = DATA_CENTER_ID)
    private String dataCenterId;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDataCenterId(String dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public String getDataCenterId() {
        return dataCenterId;
    }

    public Cluster() {
    }

    public Cluster(String id, String name) {
        super(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Cluster cluster = (Cluster) o;

        if (!ObjectUtils.equals(version, cluster.version)) return false;
        if (!ObjectUtils.equals(dataCenterId, cluster.dataCenterId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (dataCenterId != null ? dataCenterId.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(VERSION, getVersion());
        values.put(DATA_CENTER_ID, getDataCenterId());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setVersion(cursorHelper.getString(VERSION));
        setDataCenterId(cursorHelper.getString(DATA_CENTER_ID));
    }
}
