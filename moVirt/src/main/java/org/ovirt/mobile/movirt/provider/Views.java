package org.ovirt.mobile.movirt.provider;

public class Views {

    /**
     * Implement to create views
     */
    static ViewHelper.View[] getViews() {
        return new ViewHelper.View[]{
                new ViewHelper.View(OVirtContract.DiskAndAttachment.TABLE, getDisksAndAttachmentsSQL())
        };
    }

    private static String getDisksAndAttachmentsSQL() {
        String disks = OVirtContract.Disk.TABLE;
        String diskId = OVirtContract.Disk.ID;

        String attachments = OVirtContract.DiskAttachment.TABLE;
        String attachmentDiskId = OVirtContract.DiskAttachment.DISK_ID;

        String disksAndAttachments = OVirtContract.DiskAndAttachment.TABLE;

        return String.format("CREATE VIEW %s AS SELECT %s FROM %s, %s WHERE %s.%s = %s.%s",
                disksAndAttachments,
                getDisksAndAttachmentsProjection(),
                disks,
                attachments,
                // where
                disks, diskId,
                attachments, attachmentDiskId);
    }

    private static String getDisksAndAttachmentsProjection() {
        String disks = OVirtContract.Disk.TABLE;
        String diskId = OVirtContract.Disk.ID;
        String diskName = OVirtContract.Disk.NAME;
        String diskStatus = OVirtContract.Disk.STATUS;
        String diskSize = OVirtContract.Disk.SIZE;
        String diskUsedSize = OVirtContract.Disk.USED_SIZE;

        String attachments = OVirtContract.DiskAttachment.TABLE;
        String attachmentsVmId = OVirtContract.DiskAttachment.VM_ID;

        String DADiskId = OVirtContract.DiskAndAttachment.ID;
        String DADiskName = OVirtContract.DiskAndAttachment.NAME;
        String DADiskStatus = OVirtContract.DiskAndAttachment.STATUS;
        String DADiskSize = OVirtContract.DiskAndAttachment.SIZE;
        String DADiskUsedSize = OVirtContract.DiskAndAttachment.USED_SIZE;
        String DAVmId = OVirtContract.DiskAndAttachment.VM_ID;

        return String.format("%s.%s AS %s, %s.%s AS %s, %s.%s AS %s, %s.%s AS %s, %s.%s AS %s, %s.%s AS %s",
                disks, diskId, DADiskId,
                disks, diskName, DADiskName,
                disks, diskStatus, DADiskStatus,
                disks, diskSize, DADiskSize,
                disks, diskUsedSize, DADiskUsedSize,
                attachments, attachmentsVmId, DAVmId);
    }
}
