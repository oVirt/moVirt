package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtNamedEntity;
import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotVm.TABLE;

@DatabaseTable(tableName = TABLE)
public class SnapshotVm extends OVirtNamedEntity implements OVirtContract.SnapshotVm {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = SNAPSHOT_ID, canBeNull = false)
    private String snapshotId;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;

    @DatabaseField(columnName = STATUS, canBeNull = false)
    private VmStatus status;

    @DatabaseField(columnName = CLUSTER_ID, canBeNull = false)
    private String clusterId;

    @DatabaseField(columnName = MEMORY_SIZE)
    private long memorySize;

    @DatabaseField(columnName = SOCKETS)
    private int sockets;

    @DatabaseField(columnName = CORES_PER_SOCKET)
    private int coresPerSocket;

    @DatabaseField(columnName = OS_TYPE)
    private String osType;

    @Override
    public String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    @Override
    public String getVmId() {
        return vmId;
    }

    @Override
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public VmStatus getStatus() {
        return status;
    }

    public void setStatus(VmStatus status) {
        this.status = status;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public long getMemorySize() {
        return memorySize;
    }

    @Override
    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    @Override
    public int getSockets() {
        return sockets;
    }

    @Override
    public void setSockets(int sockets) {
        this.sockets = sockets;
    }

    @Override
    public int getCoresPerSocket() {
        return coresPerSocket;
    }

    @Override
    public void setCoresPerSocket(int coresPerSocket) {
        this.coresPerSocket = coresPerSocket;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SnapshotVm vm = (SnapshotVm) o;

        if (!ObjectUtils.equals(snapshotId, vm.snapshotId)) return false;
        if (!ObjectUtils.equals(vmId, vm.vmId)) return false;
        if (coresPerSocket != vm.coresPerSocket) return false;
        if (memorySize != vm.memorySize) return false;
        if (sockets != vm.sockets) return false;
        if (!ObjectUtils.equals(clusterId, vm.clusterId)) return false;
        if (!ObjectUtils.equals(osType, vm.osType)) return false;
        if (status != vm.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (snapshotId != null ? snapshotId.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
        result = 31 * result + (int) (memorySize ^ (memorySize >>> 32));
        result = 31 * result + sockets;
        result = 31 * result + coresPerSocket;
        result = 31 * result + (osType != null ? osType.hashCode() : 0);
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(SNAPSHOT_ID, getSnapshotId());
        contentValues.put(VM_ID, getVmId());
        contentValues.put(STATUS, getStatus().toString());
        contentValues.put(CLUSTER_ID, getClusterId());
        contentValues.put(MEMORY_SIZE, getMemorySize());
        contentValues.put(SOCKETS, getSockets());
        contentValues.put(CORES_PER_SOCKET, getCoresPerSocket());
        contentValues.put(OS_TYPE, getOsType());
        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setSnapshotId(cursorHelper.getString(SNAPSHOT_ID));
        setVmId(cursorHelper.getString(VM_ID));
        setStatus(cursorHelper.getEnum(STATUS, VmStatus.class));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
        setMemorySize(cursorHelper.getLong(MEMORY_SIZE));
        setSockets(cursorHelper.getInt(SOCKETS));
        setCoresPerSocket(cursorHelper.getInt(CORES_PER_SOCKET));
        setOsType(cursorHelper.getString(OS_TYPE));
    }
}
