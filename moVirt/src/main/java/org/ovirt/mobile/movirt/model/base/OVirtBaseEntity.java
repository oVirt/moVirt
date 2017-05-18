package org.ovirt.mobile.movirt.model.base;

import android.content.ContentValues;

import com.j256.ormlite.field.DatabaseField;

import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public abstract class OVirtBaseEntity extends OVirtEntity {

    @DatabaseField(columnName = ID, id = true)
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OVirtBaseEntity)) return false;

        OVirtBaseEntity that = (OVirtBaseEntity) o;

        if (!ObjectUtils.equals(id, that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ID, getId());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        setId(cursorHelper.getString(ID));
    }
}
