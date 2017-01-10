package org.ovirt.mobile.movirt.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static org.ovirt.mobile.movirt.Constants.APP_PACKAGE_DOT;

public interface OVirtContract {

    String CONTENT_AUTHORITY = APP_PACKAGE_DOT + "provider";

    Uri BASE_CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(CONTENT_AUTHORITY).build();

    String ROW_ID = "rowid";

    interface BaseEntity extends BaseColumns {
        String ID = _ID;
    }

    interface NamedEntity extends BaseEntity {
        String NAME = "name";
    }

    interface SnapshotEmbeddableEntity extends NamedEntity {
        String SNAPSHOT_ID = "snapshot_id";
    }

    interface HasStatus {
        String STATUS = "status";
    }

    interface HasCluster {
        String CLUSTER_ID = "cluster_id";
    }

    interface HasHost {
        String HOST_ID = "host_id";
    }

    interface HasVmAbstract {
        String getVmId();

        void setVmId(String vmId);
    }

    interface HasVm extends HasVmAbstract {
        String VM_ID = "vm_id";
    }

    interface HasStorageDomain {
        String STORAGE_DOMAIN_ID = "storage_domain_id";

        String getStorageDomainId();

        void setStorageDomainId(String storageDomain);
    }

    interface HasDisk {
        String DISK_ID = "disk_id";

        String getDiskId();

        void setDiskId(String diskId);
    }

    interface HasDataCenter {
        String DATA_CENTER_ID = "data_center_id";
    }

    interface HasCpuUsage {
        String CPU_USAGE = "cpu_usage";

        double getCpuUsage();

        void setCpuUsage(double cpuUsage);
    }

    interface HasMemory {
        String MEMORY_USAGE = "mem_usage";
        String USED_MEMORY_SIZE = "used_mem_size";
        String MEMORY_SIZE = "mem_size";

        double getMemoryUsage();

        void setMemoryUsage(double memoryUsage);

        long getUsedMemorySize();

        void setUsedMemorySize(long usedMemorySize);

        long getMemorySize();

        void setMemorySize(long memorySize);
    }

    interface HasAvailableSize {
        String AVAILABLE_SIZE = "available_size";

        void setAvailableSize(long availableSize);

        long getAvailableSize();
    }

    interface HasUsedSize {
        String USED_SIZE = "used_size";

        void setUsedSize(long usedSize);

        long getUsedSize();
    }

    interface HasSize {
        String SIZE = "size";

        void setSize(long size);

        long getSize();
    }

    interface HasSockets {
        String SOCKETS = "sockets";

        void setSockets(int sockets);

        int getSockets();
    }

    interface HasCoresPerSocket {
        String CORES_PER_SOCKET = "cores_per_socket";

        void setCoresPerSocket(int coresPerSocket);

        int getCoresPerSocket();
    }

    String PATH_VMS = "vms";
    String PATH_VM = "vms/*";

    interface Vm extends NamedEntity, HasStatus, HasCluster, HasHost, SnapshotEmbeddableEntity, HasCpuUsage, HasMemory, HasSockets, HasCoresPerSocket {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VMS).build();

        String TABLE = "vms";
        String OS_TYPE = "os_type";
    }

    String PATH_HOSTS = "hosts";
    String PATH_HOST = "hosts/*";

    interface Host extends NamedEntity, HasStatus, HasCluster, HasCpuUsage, HasMemory, HasSockets, HasCoresPerSocket {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HOSTS).build();

        String TABLE = "hosts";

        String THREADS_PER_CORE = "threads_per_core";
        String OS_VERSION = "os_version";
        String ADDRESS = "address";
        String ACTIVE = "active";
        String MIGRATING = "migrating";
        String TOTAL = "total";
        String CPU_SPEED = "cpu_speed";
    }

    String PATH_CLUSTERS = "clusters";
    String PATH_CLUSTER = "clusters/*";

    interface Cluster extends NamedEntity, HasDataCenter {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CLUSTERS).build();

        String TABLE = "clusters";
        String VERSION = "version";
    }

    String PATH_DATA_CENTERS = "datacenters";
    String PATH_DATA_CENTER = "datacenters/*";

    interface DataCenter extends NamedEntity, HasStatus {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DATA_CENTERS).build();

        String TABLE = "datacenters";
        String VERSION = "version";
    }

    String PATH_STORAGE_DOMAINS = "storagedomains";
    String PATH_STORAGE_DOMAIN = "storagedomains/*";

    interface StorageDomain extends NamedEntity, HasAvailableSize, HasUsedSize {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STORAGE_DOMAINS).build();

        String TABLE = "storagedomains";
        String TYPE = "type";
        String STATUS = "status";
        String STORAGE_ADDRESS = "storage_address";
        String STORAGE_TYPE = "storage_type";
        String STORAGE_PATH = "storage_path";
        String STORAGE_FORMAT = "storage_format";
    }

    String PATH_TRIGGERS = "triggers";
    String PATH_TRIGGER = "triggers/#";

    interface Trigger extends BaseEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIGGERS).build();

        String TABLE = "triggers";

        String CONDITION = "condition";
        String NOTIFICATION = "notification";
        String SCOPE = "scope";
        String TARGET_ID = "target_id";
        String ENTITY_TYPE = "entity_type";
    }

    String PATH_EVENTS = "events";
    String PATH_EVENT = "events/#";

    interface Event extends BaseEntity, HasHost, HasCluster, HasDataCenter, HasStorageDomain {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        String TABLE = "events";

        String DESCRIPTION = "description";
        String SEVERITY = "severity";
        String TIME = "time";
        String VM_ID = "vm_id";
    }

    String PATH_CONNECTION_INFOS = "connectioninfos";
    String PATH_CONNECTION_INFO = "connectioninfos/#";

    interface ConnectionInfo extends BaseEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONNECTION_INFOS).build();

        String TABLE = "connectioninfos";

        String STATE = "state";
        String ATTEMPT = "attempt";
        String SUCCESSFUL = "successful";
        String DESCRIPTION = "description";
    }

    String PATH_SNAPSHOTS = "snapshots";
    String PATH_SNAPSHOT = "snapshots/*";

    interface Snapshot extends HasVm, NamedEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SNAPSHOTS).build();

        String TABLE = "snapshots";

        String SNAPSHOT_STATUS = "snapshot_status";
        String TYPE = "type";
        String DATE = "date";
        String PERSIST_MEMORYSTATE = "persist_memorystate";
    }

    String PATH_DISKS = "disks";
    String PATH_DISK = "disks/*";

    interface Disk extends HasVm, NamedEntity, HasStatus, SnapshotEmbeddableEntity, HasSize, HasUsedSize {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DISKS).build();

        String TABLE = "disks";
    }

    String PATH_NICS = "nics";
    String PATH_NIC = "nics/*";

    interface Nic extends HasVm, NamedEntity, SnapshotEmbeddableEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NICS).build();

        String TABLE = "nics";

        String LINKED = "linked";
        String MAC_ADDRESS = "mac_address";
        String PLUGGED = "plugged";
    }

    String PATH_CONSOLES = "consoles";
    String PATH_CONSOLE = "consoles/*";

    interface Console extends BaseEntity, HasVm {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONSOLES).build();

        String TABLE = "consoles";

        String PROTOCOL = "protocol";
    }

    String PATH_DISK_ATTACHMENTS = "disk_attachments";
    String PATH_DISK_ATTACHMENT = "disk_attachments/*";

    interface DiskAttachment extends BaseEntity, HasVm, HasDisk {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DISK_ATTACHMENTS).build();

        String TABLE = "disk_attachments";
    }

    // Views
    String PATH_DISKS_AND_ATTACHMENTS = "disks_and_attachments";
    String PATH_DISKS_AND_ATTACHMENT = "disks_and_attachments/*";

    interface DiskAndAttachment extends NamedEntity, HasVm, HasStatus, HasSize, HasUsedSize {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DISKS_AND_ATTACHMENTS).build();

        String TABLE = "disks_and_attachments";
    }
}
