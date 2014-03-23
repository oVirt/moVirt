package org.ovirt.mobile.movirt.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class OVirtContract {
    private OVirtContract() {}

    public static final String CONTENT_AUTHORITY = "org.ovirt.mobile.movirt.provider";

    public static final Uri BASE_CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(CONTENT_AUTHORITY).build();

    public static final String PATH_VMS = "vms";
    public static final String PATH_VM = "vms/*";

    public static interface NamedEntity extends BaseColumns {
        public static final String NAME = "name";
    }

    public static class Vm implements NamedEntity {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VMS).build();

        public static final String TABLE = "vms";

        public static final String STATUS = "status";
        public static final String CLUSTER_ID = "cluster_id";
        public static final String CPU_USAGE = "cpu_usage";
        public static final String MEMORY_USAGE = "mem_usage";

        public static final String[] ALL_COLUMNS = {
                _ID,
                NAME,
                STATUS,
                CLUSTER_ID,
                CPU_USAGE,
                MEMORY_USAGE
        };
    }

    public static final String PATH_CLUSTERS = "clusters";
    public static final String PATH_CLUSTER = "clusters/*";

    public static class Cluster implements NamedEntity {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CLUSTERS).build();

        public static final String TABLE = "clusters";
    }

    public static final String PATH_TRIGGERS = "triggers";
    public static final String PATH_TRIGGER = "triggers/#";

    public static class Trigger implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIGGERS).build();

        public static final String TABLE = "triggers";

        public static final String CONDITION = "condition";
        public static final String NOTIFICATION = "notification";
        public static final String SCOPE = "scope";
        public static final String TARGET_ID = "target_id";
        public static final String ENTITY_TYPE = "entity_type";
    }

    public static final String PATH_EVENTS = "events";
    public static final String PATH_EVENT = "events/#";

    public static class Event implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        public static final String TABLE = "events";

        public static final String DESCRIPTION = "description";
        public static final String SEVERITY = "severity";
        public static final String TIME = "time";
        public static final String VM_ID = "vm_id";
        public static final String HOST_ID = "host_id";
        public static final String CLUSTER_ID = "cluster_id";
        public static final String STORAGE_DOMAIN_ID = "storage_domain_id";
        public static final String DATA_CENTER_ID = "data_center_id";
    }
}