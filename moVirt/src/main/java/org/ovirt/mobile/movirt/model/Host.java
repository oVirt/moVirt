package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

public class Host extends OVirtEntity implements OVirtContract.Host {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Status {
        DOWN(R.drawable.down),
        ERROR(R.drawable.error),
        INITIALIZING(R.drawable.wait),
        INSTALLING(R.drawable.host_installing),
        INSTALL_FAILED(R.drawable.down),
        MAINTENANCE(R.drawable.host_maintenance),
        NON_OPERATIONAL(R.drawable.nonoperational),
        NON_RESPONSIVE(R.drawable.down),
        PENDING_APPROVAL(R.drawable.unconfigured),
        PREPARING_FOR_MAINTENANCE(R.drawable.host_prepare_to_migrate),
        CONNECTING(R.drawable.down),
        REBOOT(R.drawable.wait),
        UNASSIGNED(R.drawable.down),
        UP(R.drawable.up),
        INSTALLING_OS(R.drawable.unconfigured),
        KDUMPING(R.drawable.wait);

        Status(int resource) {
            this.resource = resource;
        }

        private final int resource;

        public int getResource() {
            return resource;
        }
    }

    @DatabaseField(columnName = STATUS, canBeNull = false)
    private Status status;

    @DatabaseField(columnName = CLUSTER_ID, canBeNull = false)
    private String clusterId;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Host host = (Host) o;

        if (!clusterId.equals(host.clusterId)) return false;
        if (status != host.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + clusterId.hashCode();
        return result;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(STATUS, getStatus().toString());
        values.put(CLUSTER_ID, getClusterId());
        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);

        setStatus(cursorHelper.getEnum(STATUS, Status.class));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
    }
}
