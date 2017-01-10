package org.ovirt.mobile.movirt.model.base;

import android.content.ContentValues;

import com.j256.ormlite.field.DatabaseField;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public abstract class OVirtNamedEntity extends OVirtBaseEntity implements OVirtContract.NamedEntity {

    protected OVirtNamedEntity() {
        super();
        name = "";
    }

    protected OVirtNamedEntity(String id) {
        super(id);
        name = "";
    }

    protected OVirtNamedEntity(String id, String name) {
        super(id);
        this.name = name;
    }

    @DatabaseField(columnName = NAME, canBeNull = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OVirtNamedEntity)) return false;
        if (!super.equals(o)) return false;

        OVirtNamedEntity that = (OVirtNamedEntity) o;

        if (!ObjectUtils.equals(name, that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(ID, getId());
        values.put(NAME, getName());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);
        setName(cursorHelper.getString(NAME));
    }
}
