package org.ovirt.mobile.movirt.model.view;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OvirtNamedViewEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.DiskAndAttachment.TABLE;

@DatabaseTable(tableName = TABLE)
public class DiskAndAttachment extends OvirtNamedViewEntity implements OVirtContract.DiskAndAttachment {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = STATUS)
    private String status;

    @DatabaseField(columnName = VM_ID)
    private String vmId;

    @DatabaseField(columnName = SIZE)
    private long size;

    @DatabaseField(columnName = USED_SIZE)
    private long usedSize;

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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(long usedSize) {
        this.usedSize = usedSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiskAndAttachment)) return false;
        if (!super.equals(o)) return false;

        DiskAndAttachment diskAndAttachment = (DiskAndAttachment) o;

        if (!ObjectUtils.equals(status, diskAndAttachment.status)) return false;
        if (!ObjectUtils.equals(vmId, diskAndAttachment.vmId)) return false;
        if (size != diskAndAttachment.size) return false;
        if (usedSize != diskAndAttachment.usedSize) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (usedSize ^ (usedSize >>> 32));

        return result;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);
        setStatus(cursorHelper.getString(OVirtContract.Disk.STATUS));
        setSize(cursorHelper.getLong(OVirtContract.Disk.SIZE));
        setUsedSize(cursorHelper.getLong(OVirtContract.Disk.USED_SIZE));
        setVmId(cursorHelper.getString(OVirtContract.DiskAttachment.VM_ID));
    }
}
