package org.ovirt.mobile.movirt.ui.dashboard;

import android.content.Context;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.OVirtEntity;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.util.ObjectUtils;

public class DashboardBoxData {
    private Class<? extends OVirtEntity> entityClass;
    private int entityCount;
    private int warningEventCount;
    private int alertEventCount;
    private int errorEventCount;

    public DashboardBoxData(Class<? extends OVirtEntity> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<? extends OVirtEntity> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<? extends OVirtEntity> entityClass) {
        this.entityClass = entityClass;
    }

    public String getEntityCountFormatStr(Context context) {
        String entityStr = "";
        if (ObjectUtils.equals(entityClass, DataCenter.class)) {
            entityStr += context.getString(R.string.data_centers);
        } else if (ObjectUtils.equals(entityClass, Cluster.class)) {
            entityStr += context.getString(R.string.clusters);
        } else if (ObjectUtils.equals(entityClass, Host.class)) {
            entityStr += context.getString(R.string.hosts);
        } else if (ObjectUtils.equals(entityClass, StorageDomain.class)) {
            entityStr += context.getString(R.string.storage_domains);
        } else if (ObjectUtils.equals(entityClass, Vm.class)) {
            entityStr += context.getString(R.string.virtual_machines);
        }

        if (entityStr.equals("")) {
            return "";
        } else {
            return entityCount + " " + entityStr;
        }
    }

    public int getWarningEventCount() {
        return warningEventCount;
    }

    public void setWarningEventCount(int warningEventCount) {
        this.warningEventCount = warningEventCount;
    }

    public int getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(int entityCount) {
        this.entityCount = entityCount;
    }

    public int getAlertEventCount() {
        return alertEventCount;
    }

    public void setAlertEventCount(int alertEventCount) {
        this.alertEventCount = alertEventCount;
    }

    public int getErrorEventCount() {
        return errorEventCount;
    }

    public void setErrorEventCount(int errorEventCount) {
        this.errorEventCount = errorEventCount;
    }
}
