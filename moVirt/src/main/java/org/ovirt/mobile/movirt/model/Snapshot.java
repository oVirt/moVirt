package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Snapshot.TABLE;

@DatabaseTable(tableName = TABLE)
public class Snapshot extends OVirtEntity implements OVirtContract.Snapshot {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = SNAPSHOT_STATUS)
    private String snapshotStatus;

    @DatabaseField(columnName = DATE)
    private long date;

    @DatabaseField(columnName = PERSIST_MEMORYSTATE)
    private boolean persistMemorystate;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;

    public String getSnapshotStatus() {
        return snapshotStatus;
    }

    public void setSnapshotStatus(String snapshot_status) {
        this.snapshotStatus = snapshot_status;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean getPersistMemorystate() {
        return persistMemorystate;
    }

    public void setPersistMemorystate(boolean persistMemorystate) {
        this.persistMemorystate = persistMemorystate;
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

        Snapshot snapshot = (Snapshot) o;

        if (!ObjectUtils.equals(snapshotStatus, snapshot.snapshotStatus)) return false;
        if (date != snapshot.date) return false;
        if (persistMemorystate != snapshot.persistMemorystate) return false;
        if (!ObjectUtils.equals(vmId, snapshot.vmId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (snapshotStatus != null ? snapshotStatus.hashCode() : 0);
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + (persistMemorystate ? 1231 : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(SNAPSHOT_STATUS, getSnapshotStatus());
        contentValues.put(DATE, getDate());
        contentValues.put(PERSIST_MEMORYSTATE, getPersistMemorystate());
        contentValues.put(VM_ID, getVmId());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setSnapshotStatus(cursorHelper.getString(SNAPSHOT_STATUS));
        setDate(cursorHelper.getLong(DATE));
        setPersistMemorystate(cursorHelper.getBoolean(PERSIST_MEMORYSTATE));
        setVmId(cursorHelper.getString(VM_ID));
    }
}
