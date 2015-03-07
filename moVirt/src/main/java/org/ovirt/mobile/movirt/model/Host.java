package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;

import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

public class Host extends OVirtEntity implements OVirtContract.Host {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    public enum Status {
        DOWN,
        ERROR,
        INITIALIZING,
        INSTALLING,
        INSTALL_FAILED,
        MAINTENANCE,
        NON_OPERATIONAL,
        NON_RESPONSIVE,
        PENDING_APPROVAL,
        PREPARING_FOR_MAINTENANCE,
        CONNECTING,
        REBOOT,
        UNASSIGNED,
        UP,
        INSTALLING_OS,
        KDUMPING
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
