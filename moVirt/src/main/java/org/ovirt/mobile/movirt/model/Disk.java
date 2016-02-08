package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Disk.TABLE;

@DatabaseTable(tableName = TABLE)
public class Disk extends SnapshotEmbeddableEntity implements OVirtContract.Disk {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = SIZE)
    private String size;

    @DatabaseField(columnName = STATUS)
    private String status;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Disk disk = (Disk) o;

        if (!ObjectUtils.equals(status, disk.status)) return false;
        if (!ObjectUtils.equals(size, disk.size)) return false;
        if (!ObjectUtils.equals(vmId, disk.vmId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(STATUS, getStatus());
        contentValues.put(SIZE, getSize());
        contentValues.put(VM_ID, getVmId());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setStatus(cursorHelper.getString(STATUS));
        setSize(cursorHelper.getString(SIZE));
        setVmId(cursorHelper.getString(VM_ID));
    }
}
