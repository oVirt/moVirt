package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;

import com.j256.ormlite.field.DatabaseField;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public abstract class OVirtEntity extends BaseEntity<String> implements OVirtContract.NamedEntity {

    public OVirtEntity() {
        id = "";
        name = "";
    }

    @DatabaseField(columnName = ID, id = true)
    private String id;

    @DatabaseField(columnName = NAME, canBeNull = false)
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OVirtEntity)) return false;

        OVirtEntity that = (OVirtEntity) o;

        if (!ObjectUtils.equals(id, that.id)) return false;
        if (!ObjectUtils.equals(name, that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ID, getId());
        values.put(NAME, getName());
        return values;
    }
}
