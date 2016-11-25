package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.DataCenter.TABLE;

@DatabaseTable(tableName = TABLE)
public class DataCenter extends OVirtEntity implements OVirtContract.DataCenter {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Status {
        UNKNOWN(-1),
        CONTEND(0),
        MAINTENANCE(1),
        NOT_OPERATIONAL(2),
        PROBLEMATIC(3),
        UNINITIALIZED(4),
        UP(5);

        Status(int resource) {
            this.resource = resource;
        }

        private final int resource;

        public int getResource() {
            return resource;
        }

    }

    @DatabaseField(columnName = VERSION)
    private String version;

    @DatabaseField(columnName = STATUS)
    private Status status;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataCenter dataCenter = (DataCenter) o;

        if (!ObjectUtils.equals(version, dataCenter.version)) return false;
        if (status != dataCenter.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(VERSION, getVersion());
        values.put(STATUS, getStatus().toString());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setVersion(cursorHelper.getString(VERSION));
        setStatus(cursorHelper.getEnum(STATUS, DataCenter.Status.class));
    }
}
