package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtBaseEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAttachment.TABLE;

@DatabaseTable(tableName = TABLE)
public class DiskAttachment extends OVirtBaseEntity implements OVirtContract.DiskAttachment {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = DISK_ID)
    private String diskId;

    @DatabaseField(columnName = VM_ID)
    private String vmId;

    @Override
    public String getDiskId() {
        return diskId;
    }

    @Override
    public void setDiskId(String diskId) {
        this.diskId = diskId;
    }

    @Override
    public String getVmId() {
        return vmId;
    }

    @Override
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DiskAttachment diskAttachment = (DiskAttachment) o;

        if (!ObjectUtils.equals(diskId, diskAttachment.diskId)) return false;
        if (!ObjectUtils.equals(vmId, diskAttachment.vmId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);
        result = 31 * result + (diskId != null ? diskId.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(DISK_ID, getDiskId());
        contentValues.put(VM_ID, getVmId());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setDiskId(cursorHelper.getString(DISK_ID));
        setVmId(cursorHelper.getString(VM_ID));
    }
}
