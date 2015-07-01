package org.ovirt.mobile.movirt.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public interface OVirtContract {

    String CONTENT_AUTHORITY = "org.ovirt.mobile.movirt.provider";

    Uri BASE_CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(CONTENT_AUTHORITY).build();

    public interface BaseEntity extends BaseColumns {
        String ID = _ID;
    }

    public interface NamedEntity extends BaseEntity {
        String NAME = "name";
    }

    public interface HasStatus {
        String STATUS = "status";
    }

    public interface HasCluster {
        String CLUSTER_ID = "cluster_id";
    }

    public interface HasHost {
        String HOST_ID = "host_id";
    }

    public interface HasDataCenter {
        String DATA_CENTER_ID = "data_center_id";
    }

    String PATH_VMS = "vms";
    String PATH_VM = "vms/*";

    public interface Vm extends NamedEntity, HasStatus, HasCluster, HasHost {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VMS).build();

        String TABLE = "vms";

        String CPU_USAGE = "cpu_usage";
        String MEMORY_USAGE = "mem_usage";
        String MEMORY_SIZE_MB = "mem_size_mb";
        String SOCKETS = "sockets";
        String CORES_PER_SOCKET = "cores_per_socket";
        String OS_TYPE = "os_type";
        String DISPLAY_TYPE = "display_type";
        String DISPLAY_ADDRESS = "display_address";
        String DISPLAY_PORT = "display_port";
        String DISPLAY_SECURE_PORT = "display_secure_port";
        String CERTIFICATE_SUBJECT = "certificate_subject";
    }

    String PATH_HOSTS = "hosts";
    String PATH_HOST = "hosts/*";

    public interface Host extends NamedEntity, HasStatus, HasCluster {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HOSTS).build();

        String TABLE = "hosts";

        String CPU_USAGE = "cpu_usage";
        String MEMORY_USAGE = "mem_usage";
        String MEMORY_SIZE_MB = "mem_size_mb";
        String SOCKETS = "sockets";
        String CORES_PER_SOCKET = "cores_per_socket";
        String THREADS_PER_CORE = "threads_per_core";
        String OS_VERSION = "os_version";
        String ADDRESS = "address";
        String ACTIVE = "active";
        String MIGRATING = "migrating";
        String TOTAL = "total";
    }

    String PATH_CLUSTERS = "clusters";
    String PATH_CLUSTER = "clusters/*";

    public interface Cluster extends NamedEntity, HasDataCenter {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CLUSTERS).build();

        String TABLE = "clusters";
        String VERSION = "version";
    }

    String PATH_DATA_CENTERS = "datacenters";
    String PATH_DATA_CENTER = "datacenters/*";

    public interface DataCenter extends NamedEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DATA_CENTERS).build();

        String TABLE = "datacenters";
        String VERSION = "version";
    }

    String PATH_TRIGGERS = "triggers";
    String PATH_TRIGGER = "triggers/#";

    public interface Trigger extends BaseEntity {
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

    public interface Event extends BaseEntity, HasHost, HasCluster, HasDataCenter {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        String TABLE = "events";

        String DESCRIPTION = "description";
        String SEVERITY = "severity";
        String TIME = "time";
        String VM_ID = "vm_id";
        String STORAGE_DOMAIN_ID = "storage_domain_id";
    }


    String PATH_CA_CRTS = "cacerts";
    public interface CaCert extends BaseEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CA_CRTS).build();

        String TABLE = "cacerts";

        String CONTENT = "content";
        String VALID_FOR = "valid_for";

    }

    String PATH_CONNECTION_INFOS = "connectioninfos";
    String PATH_CONNECTION_INFO = "connectioninfos/#";
    interface ConnectionInfo extends BaseEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONNECTION_INFOS).build();

        String TABLE = "connectioninfos";

        String STATE = "state";
        String ATTEMPT = "attempt";
        String SUCCESSFUL = "successful";
    }
}