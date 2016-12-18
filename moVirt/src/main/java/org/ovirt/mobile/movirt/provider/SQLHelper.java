package org.ovirt.mobile.movirt.provider;

public class SQLHelper {
    public static String getDisksAndAttachmentsInnerJoinSQL() {
        String disks = OVirtContract.Disk.TABLE;
        String diskId = OVirtContract.Disk.ID;

        String attachments = OVirtContract.DiskAttachment.TABLE;
        String attachmentDiskId = OVirtContract.DiskAttachment.DISK_ID;

        return String.format("%s INNER JOIN %s ON (%s.%s = %s.%s)",
                disks,
                attachments,
                // on
                disks, diskId,
                attachments, attachmentDiskId);
    }

    public static String[] getDisksAndAttachmentsProjection() {
        String disks = OVirtContract.Disk.TABLE;
        String diskId = OVirtContract.Disk.ID;
        String diskName = OVirtContract.Disk.NAME;
        String diskStatus = OVirtContract.Disk.STATUS;
        String diskSize = OVirtContract.Disk.SIZE;
        String diskUsedSize = OVirtContract.Disk.USED_SIZE;

        String attachments = OVirtContract.DiskAttachment.TABLE;
        String attachmentsVmId = OVirtContract.DiskAttachment.VM_ID;

        return new String[]{
                String.format("%s.%s as %s", attachments, OVirtContract.ROW_ID, OVirtContract.ROW_ID),
                String.format("%s.%s", disks, diskId),
                String.format("%s.%s", attachments, attachmentsVmId),
                String.format("%s.%s", disks, diskName),
                String.format("%s.%s", disks, diskStatus),
                String.format("%s.%s", disks, diskSize),
                String.format("%s.%s", disks, diskUsedSize)
        };
    }
}
