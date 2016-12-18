package org.ovirt.mobile.movirt.model.view;

import org.ovirt.mobile.movirt.model.base.OvirtViewEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public class DiskAndAttachment extends OvirtViewEntity implements OVirtContract.DiskAndAttachment {

    private String id;

    private String name;

    private String status;

    private String vmId;

    private long size;

    private long usedSize;

    public DiskAndAttachment() {
    }

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

        DiskAndAttachment diskAndAttachment = (DiskAndAttachment) o;

        if (!ObjectUtils.equals(id, diskAndAttachment.id)) return false;
        if (!ObjectUtils.equals(name, diskAndAttachment.name)) return false;
        if (!ObjectUtils.equals(status, diskAndAttachment.status)) return false;
        if (!ObjectUtils.equals(vmId, diskAndAttachment.vmId)) return false;
        if (size != diskAndAttachment.size) return false;
        if (usedSize != diskAndAttachment.usedSize) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (usedSize ^ (usedSize >>> 32));

        return result;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        setId(cursorHelper.getString(OVirtContract.Disk.ID));
        setName(cursorHelper.getString(OVirtContract.Disk.NAME));
        setStatus(cursorHelper.getString(OVirtContract.Disk.STATUS));
        setSize(cursorHelper.getLong(OVirtContract.Disk.SIZE));
        setUsedSize(cursorHelper.getLong(OVirtContract.Disk.USED_SIZE));
        setVmId(cursorHelper.getString(OVirtContract.DiskAttachment.VM_ID));
    }
}
