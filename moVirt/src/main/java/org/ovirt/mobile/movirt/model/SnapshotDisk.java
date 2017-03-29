package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtNamedEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotDisk.TABLE;

@DatabaseTable(tableName = TABLE)
public class SnapshotDisk extends OVirtNamedEntity implements OVirtContract.SnapshotDisk {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = DISK_ID, canBeNull = false)
    private String diskId;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;

    @DatabaseField(columnName = SNAPSHOT_ID, canBeNull = false)
    private String snapshotId;

    @DatabaseField(columnName = STATUS)
    private String status;

    @DatabaseField(columnName = SIZE)
    private long size;

    @DatabaseField(columnName = USED_SIZE)
    private long usedSize;

    @Override
    public String getDiskId() {
        return diskId;
    }

    @Override
    public void setDiskId(String diskId) {
        this.diskId = diskId;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public long getUsedSize() {
        return usedSize;
    }

    @Override
    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SnapshotDisk disk = (SnapshotDisk) o;

        if (!ObjectUtils.equals(diskId, disk.diskId)) return false;
        if (!ObjectUtils.equals(vmId, disk.vmId)) return false;
        if (!ObjectUtils.equals(snapshotId, disk.snapshotId)) return false;
        if (!ObjectUtils.equals(status, disk.status)) return false;
        if (usedSize != disk.usedSize) return false;
        if (size != disk.size) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (diskId != null ? diskId.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);
        result = 31 * result + (snapshotId != null ? snapshotId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (int) (usedSize ^ (usedSize >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();

        contentValues.put(DISK_ID, getDiskId());
        contentValues.put(VM_ID, getVmId());
        contentValues.put(SNAPSHOT_ID, getSnapshotId());
        contentValues.put(STATUS, getStatus());
        contentValues.put(USED_SIZE, getUsedSize());
        contentValues.put(SIZE, getSize());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setDiskId(cursorHelper.getString(DISK_ID));
        setVmId(cursorHelper.getString(VM_ID));
        setSnapshotId(cursorHelper.getString(SNAPSHOT_ID));
        setStatus(cursorHelper.getString(STATUS));
        setUsedSize(cursorHelper.getLong(USED_SIZE));
        setSize(cursorHelper.getLong(SIZE));
    }
}
