package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

import static org.ovirt.mobile.movirt.provider.OVirtContract.StorageDomain.TABLE;

@DatabaseTable(tableName = TABLE)
public class StorageDomain extends OVirtEntity implements OVirtContract.StorageDomain {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Type {
        DATA,
        ISO,
        IMAGE,
        EXPORT
    }

    @DatabaseField(columnName = TYPE)
    private Type type;

    @DatabaseField(columnName = AVAILABLE_SIZE_MB)
    private long availableSizeMb;

    @DatabaseField(columnName = USED_SIZE_MB)
    private long usedSizeMb;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getAvailableSizeMb() {
        return availableSizeMb;
    }

    public void setAvailableSizeMb(long availableSizeMb) {
        this.availableSizeMb = availableSizeMb;
    }

    public long getUsedSizeMb() {
        return usedSizeMb;
    }

    public void setUsedSizeMb(long usedSizeMb) {
        this.usedSizeMb = usedSizeMb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StorageDomain storageDomain = (StorageDomain) o;

        if (type != storageDomain.type) return false;
        if (availableSizeMb != storageDomain.availableSizeMb) return false;
        if (usedSizeMb != storageDomain.usedSizeMb) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (availableSizeMb ^ (availableSizeMb >>> 32));
        result = 31 * result + (int) (usedSizeMb ^ (usedSizeMb >>> 32));

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(TYPE, getType().toString());
        values.put(AVAILABLE_SIZE_MB, getAvailableSizeMb());
        values.put(USED_SIZE_MB, getUsedSizeMb());

        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setType(cursorHelper.getEnum(TYPE, StorageDomain.Type.class));
        setAvailableSizeMb(cursorHelper.getLong(AVAILABLE_SIZE_MB));
        setUsedSizeMb(cursorHelper.getLong(USED_SIZE_MB));
    }

}
