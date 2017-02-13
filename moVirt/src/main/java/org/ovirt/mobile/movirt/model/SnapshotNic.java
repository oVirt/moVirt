package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtNamedEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotNic.TABLE;

@DatabaseTable(tableName = TABLE)
public class SnapshotNic extends OVirtNamedEntity implements OVirtContract.SnapshotNic {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = NIC_ID, canBeNull = false)
    private String nicId;

    @DatabaseField(columnName = SNAPSHOT_ID, canBeNull = false)
    private String snapshotId;

    @DatabaseField(columnName = VM_ID, canBeNull = false)
    private String vmId;

    @DatabaseField(columnName = LINKED)
    private boolean linked;

    @DatabaseField(columnName = MAC_ADDRESS)
    private String macAddress;

    @DatabaseField(columnName = PLUGGED)
    private boolean plugged;

    @Override
    public String getNicId() {
        return nicId;
    }

    @Override
    public void setNicId(String nicId) {
        this.nicId = nicId;
    }

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

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        this.plugged = plugged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SnapshotNic nic = (SnapshotNic) o;

        if (!ObjectUtils.equals(nicId, nic.nicId)) return false;
        if (!ObjectUtils.equals(snapshotId, nic.snapshotId)) return false;
        if (!ObjectUtils.equals(vmId, nic.vmId)) return false;
        if (linked != nic.linked) return false;
        if (!ObjectUtils.equals(macAddress, nic.macAddress)) return false;
        if (plugged != nic.plugged) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (nicId != null ? nicId.hashCode() : 0);
        result = 31 * result + (snapshotId != null ? snapshotId.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);
        result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
        result = 31 * result + (linked ? 1231 : 0);
        result = 31 * result + (plugged ? 1231 : 0);

        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues contentValues = super.toValues();
        contentValues.put(NIC_ID, getNicId());
        contentValues.put(SNAPSHOT_ID, getSnapshotId());
        contentValues.put(VM_ID, getVmId());
        contentValues.put(MAC_ADDRESS, getMacAddress());
        contentValues.put(LINKED, isLinked());
        contentValues.put(PLUGGED, isPlugged());

        return contentValues;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setNicId(cursorHelper.getString(NIC_ID));
        setSnapshotId(cursorHelper.getString(SNAPSHOT_ID));
        setVmId(cursorHelper.getString(VM_ID));
        setMacAddress(cursorHelper.getString(MAC_ADDRESS));
        setLinked(cursorHelper.getBoolean(LINKED));
        setPlugged(cursorHelper.getBoolean(PLUGGED));
    }
}
