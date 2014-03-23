package org.ovirt.mobile.movirt.model;

import com.j256.ormlite.field.DatabaseField;

import java.sql.Timestamp;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.*;

public class Event {

    public static final class Codes {
        public static final int USER_VDC_LOGIN = 30;
        public static final int USER_VDC_LOGOUT = 31;
    }

    public enum Severity {
        NORMAL,
        WARNING,
        ERROR,
        ALERT
    }

    @DatabaseField(columnName = _ID, id = true)
    private int id;

    @DatabaseField(columnName = DESCRIPTION, canBeNull = false)
    private String description;

    @DatabaseField(columnName = SEVERITY, canBeNull = false)
    private Severity severity;

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

    private transient int code;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
