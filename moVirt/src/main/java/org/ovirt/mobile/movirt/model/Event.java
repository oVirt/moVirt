package org.ovirt.mobile.movirt.model;

import android.content.ContentValues;
import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.ovirt.mobile.movirt.model.base.OVirtAccountEntity;
import org.ovirt.mobile.movirt.model.enums.EventSeverity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.util.CursorHelper;

import java.sql.Timestamp;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.TABLE;

@DatabaseTable(tableName = TABLE)
public class Event extends OVirtAccountEntity implements OVirtContract.Event {

    @Override
    public Uri getBaseUri() {
        return CONTENT_URI;
    }

    @DatabaseField(columnName = DESCRIPTION, canBeNull = false)
    private String description;

    @DatabaseField(columnName = SEVERITY, canBeNull = false)
    private EventSeverity severity;

    @DatabaseField(columnName = TIME, canBeNull = false)
    private Timestamp time;

    @DatabaseField(columnName = VM_ID)
    private String vmId;

    @DatabaseField(columnName = HOST_ID)
    private String hostId;

    @DatabaseField(columnName = CLUSTER_ID)
    private String clusterId;

    @DatabaseField(columnName = STORAGE_DOMAIN_ID)
    private String storageDomainId;

    @DatabaseField(columnName = DATA_CENTER_ID)
    private String dataCenterId;

    @DatabaseField(columnName = TEMPORARY)
    private boolean temporary;

    public static final class Codes {

        public static final int USER_VDC_LOGIN = 30;
        public static final int USER_VDC_LOGOUT = 31;
    }

    private transient int code;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(EventSeverity severity) {
        this.severity = severity;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(String storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public void setDataCenterId(String dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public String getDataCenterId() {
        return dataCenterId;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ContentValues toValues() {
        ContentValues values = super.toValues();
        values.put(DESCRIPTION, description);
        values.put(SEVERITY, severity.toString());
        values.put(TIME, time.toString());
        values.put(VM_ID, vmId);
        values.put(HOST_ID, hostId);
        values.put(CLUSTER_ID, clusterId);
        values.put(STORAGE_DOMAIN_ID, storageDomainId);
        values.put(DATA_CENTER_ID, dataCenterId);
        values.put(TEMPORARY, temporary);

        return values;
    }

    @Override
    public void initFromCursorHelper(CursorHelper cursorHelper) {
        super.initFromCursorHelper(cursorHelper);
        setDescription(cursorHelper.getString(DESCRIPTION));
        setSeverity(cursorHelper.getEnum(SEVERITY, EventSeverity.class));
        setTime(cursorHelper.getTimestamp(TIME));
        setVmId(cursorHelper.getString(VM_ID));
        setHostId(cursorHelper.getString(HOST_ID));
        setClusterId(cursorHelper.getString(CLUSTER_ID));
        setStorageDomainId(cursorHelper.getString(STORAGE_DOMAIN_ID));
        setDataCenterId(cursorHelper.getString(DATA_CENTER_ID));
        setTemporary(cursorHelper.getBoolean(TEMPORARY));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        if (!super.equals(o)) return false;

        Event event = (Event) o;

        if (temporary != event.temporary) return false;
        if (description != null ? !description.equals(event.description) : event.description != null)
            return false;
        if (severity != event.severity) return false;
        if (time != null ? !time.equals(event.time) : event.time != null) return false;
        if (vmId != null ? !vmId.equals(event.vmId) : event.vmId != null) return false;
        if (hostId != null ? !hostId.equals(event.hostId) : event.hostId != null) return false;
        if (clusterId != null ? !clusterId.equals(event.clusterId) : event.clusterId != null)
            return false;
        if (storageDomainId != null ? !storageDomainId.equals(event.storageDomainId) : event.storageDomainId != null)
            return false;
        return dataCenterId != null ? dataCenterId.equals(event.dataCenterId) : event.dataCenterId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (severity != null ? severity.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (vmId != null ? vmId.hashCode() : 0);
        result = 31 * result + (hostId != null ? hostId.hashCode() : 0);
        result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
        result = 31 * result + (storageDomainId != null ? storageDomainId.hashCode() : 0);
        result = 31 * result + (dataCenterId != null ? dataCenterId.hashCode() : 0);
        result = 31 * result + (temporary ? 1 : 0);
        return result;
    }
}
