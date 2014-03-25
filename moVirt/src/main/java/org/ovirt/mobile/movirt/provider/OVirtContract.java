package org.ovirt.mobile.movirt.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public interface OVirtContract {

    String CONTENT_AUTHORITY = "org.ovirt.mobile.movirt.provider";

    Uri BASE_CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(CONTENT_AUTHORITY).build();

    String PATH_VMS = "vms";
    String PATH_VM = "vms/*";

    public interface BaseEntity extends BaseColumns {
        String ID = _ID;
    }

    public interface NamedEntity extends BaseEntity {
        String NAME = "name";
    }

    public interface Vm extends NamedEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VMS).build();

        String TABLE = "vms";

        String STATUS = "status";
        String CLUSTER_ID = "cluster_id";
        String CPU_USAGE = "cpu_usage";
        String MEMORY_USAGE = "mem_usage";

        String[] ALL_COLUMNS = {
                ID,
                NAME,
                STATUS,
                CLUSTER_ID,
                CPU_USAGE,
                MEMORY_USAGE
        };
    }

    String PATH_CLUSTERS = "clusters";
    String PATH_CLUSTER = "clusters/*";

    public interface Cluster extends NamedEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CLUSTERS).build();

        String TABLE = "clusters";
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

    public interface Event extends BaseEntity {
        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        String TABLE = "events";

        String DESCRIPTION = "description";
        String SEVERITY = "severity";
        String TIME = "time";
        String VM_ID = "vm_id";
        String HOST_ID = "host_id";
        String CLUSTER_ID = "cluster_id";
        String STORAGE_DOMAIN_ID = "storage_domain_id";
        String DATA_CENTER_ID = "data_center_id";
    }
}