package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Snapshot.TABLE;

@DatabaseTable(tableName = TABLE)
public class Snapshot extends OVirtEntity implements OVirtContract.Snapshot {

    public enum SnapshotType {
        REGULAR,
        ACTIVE,
        STATELESS,
        PREVIEW
    }

    public enum SnapshotStatus {
        OK,
        LOCKED,
        IN_PREVIEW
    }

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = SNAPSHOT_STATUS)
    private SnapshotStatus snapshotStatus;

    @DatabaseField(columnName = TYPE)
    private SnapshotType type;

    @DatabaseField(columnName = DATE)
    private long date;

    @DatabaseField(columnName = PERSIST_MEMORYSTATE)
    private boolean persistMemorystate;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;

    // vm in a time of a snapshot
    private transient Vm vm;

    public SnapshotStatus getSnapshotStatus() {
        return snapshotStatus;
    }

    public void setSnapshotStatus(SnapshotStatus snapshot_status) {
        this.snapshotStatus = snapshot_status;
    }

    public SnapshotType getType() {
        return type;
    }

    public void setType(SnapshotType type) {
        this.type = type;
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

    public Vm getVm() {
        return vm;
    }

    public void setVm(Vm vm) {
        this.vm = vm;
    }

    public static boolean containsOneOfStatuses(Collection<Snapshot> snapshots, SnapshotStatus... statuses) {
        if (statuses.length == 0) {
            return false;
        }
        Set<SnapshotStatus> statusSet = new HashSet<>(Arrays.asList(statuses));

        for (Snapshot snapshot : snapshots) {
            if (statusSet.contains(snapshot.getSnapshotStatus())){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Snapshot snapshot = (Snapshot) o;

        if (!ObjectUtils.equals(snapshotStatus, snapshot.snapshotStatus)) return false;
        if (!ObjectUtils.equals(type, snapshot.type)) return false;
        if (date != snapshot.date) return false;
        if (persistMemorystate != snapshot.persistMemorystate) return false;
        if (!ObjectUtils.equals(vmId, snapshot.vmId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (snapshotStatus != null ? snapshotStatus.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + (persistMemorystate ? 1231 : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(SNAPSHOT_STATUS, getSnapshotStatus().toString());
        contentValues.put(TYPE, getType().toString());
        contentValues.put(DATE, getDate());
        contentValues.put(PERSIST_MEMORYSTATE, getPersistMemorystate());
        contentValues.put(VM_ID, getVmId());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);
        setSnapshotStatus(cursorHelper.getEnum(SNAPSHOT_STATUS, SnapshotStatus.class));
        setType(cursorHelper.getEnum(TYPE, SnapshotType.class));
        setDate(cursorHelper.getLong(DATE));
        setPersistMemorystate(cursorHelper.getBoolean(PERSIST_MEMORYSTATE));
        setVmId(cursorHelper.getString(VM_ID));
    }
}
