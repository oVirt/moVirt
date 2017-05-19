package org.ovirt.mobile.movirt.ui.dashboard.boxfragment;

import android.content.Context;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.DataCenter;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Host;
import org.ovirt.mobile.movirt.model.StorageDomain;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.enums.EventSeverity;
import org.ovirt.mobile.movirt.ui.dashboard.DashboardEntityStatus;
import org.ovirt.mobile.movirt.ui.dashboard.maps.DashboardPosition;
import org.ovirt.mobile.movirt.ui.dashboard.maps.DcStatusMap;
import org.ovirt.mobile.movirt.ui.dashboard.maps.HostStatusMap;
import org.ovirt.mobile.movirt.ui.dashboard.maps.StorageStatusMap;
import org.ovirt.mobile.movirt.ui.dashboard.maps.VmStatusMap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

public class DashboardBoxData {
    private BoxDataEntity entityClass;
    private int entityCount = 0;

    private Map<DashboardPosition, DashboardEntityStatus> positionsStatusMap = new EnumMap<>(DashboardPosition.class);

    public DashboardBoxData(BoxDataEntity entityClass, Collection data) {
        this(entityClass);
        setData(data);
    }

    public DashboardBoxData(BoxDataEntity entityClass) {
        this.entityClass = entityClass;

        DashboardEntityStatus first = new DashboardEntityStatus();
        DashboardEntityStatus second = new DashboardEntityStatus();
        DashboardEntityStatus third = new DashboardEntityStatus();

        positionsStatusMap.put(DashboardPosition.FIRST, first);
        positionsStatusMap.put(DashboardPosition.SECOND, second);
        positionsStatusMap.put(DashboardPosition.THIRD, third);

        switch (entityClass) {
            case DATA_CENTER:
            case CLUSTER:
            case STORAGE_DOMAIN:
            case HOST:
            case VM:
                first.setIconResourceId(R.drawable.dashboard_warning_white, R.drawable.dashboard_warning);
                second.setIconResourceId(R.drawable.dashboard_circle_up);
                third.setIconResourceId(R.drawable.dashboard_circle_down);
                break;
            case EVENT:
                first.setIconResourceId(R.drawable.dashboard_bell);
                second.setIconResourceId(R.drawable.dashboard_warning_white, R.drawable.dashboard_warning);
                third.setIconResourceId(R.drawable.dashboard_circle_error_white, R.drawable.dashboard_circle_error);
                break;
        }
    }

    public BoxDataEntity getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(BoxDataEntity entityClass) {
        this.entityClass = entityClass;
    }

    public String getEntityCountFormatStr(Context context) {
        int stringId = 0;

        switch (entityClass) {
            case DATA_CENTER:
                stringId = R.string.data_centers;
                break;
            case CLUSTER:
                stringId = R.string.clusters;
                break;
            case STORAGE_DOMAIN:
                stringId = R.string.storage_domains;
                break;
            case HOST:
                stringId = R.string.hosts;
                break;
            case VM:
                stringId = R.string.virtual_machines;
                break;
            case EVENT:
                stringId = R.string.events;
                break;
        }

        return stringId == 0 ? "" : String.format("%d %s", entityCount, context.getString(stringId));
    }

    public int getEntityImageId() {
        switch (entityClass) {
            case DATA_CENTER:
                return R.drawable.dashboard_globe;
            case CLUSTER:
                return R.drawable.dashboard_cluster;
            case STORAGE_DOMAIN:
                return R.drawable.dashboard_storage_domain;
            case HOST:
                return R.drawable.dashboard_screen;
            case VM:
                return R.drawable.dashboard_vm;
            case EVENT:
                return R.drawable.dashboard_flag;
            default:
                return R.drawable.vm_question_mark;
        }
    }

    public int getEntityCount() {
        return entityCount;
    }

    public void setData(Collection data) {
        if (entityClass != BoxDataEntity.EVENT) {
            entityCount = data.size();
        }

        if (entityClass == BoxDataEntity.CLUSTER) {
            return;
        }
        Date today = getToday();

        for (Object entity : data) {
            DashboardPosition position;
            switch (entityClass) {
                case DATA_CENTER:
                    position = DcStatusMap.getDashboardPosition(((DataCenter) entity).getStatus());
                    break;
                case STORAGE_DOMAIN:
                    position = StorageStatusMap.getDashboardPosition(((StorageDomain) entity).getStatus());
                    break;
                case HOST:
                    position = HostStatusMap.getDashboardPosition(((Host) entity).getStatus());
                    break;
                case VM:
                    position = VmStatusMap.getDashboardPosition(((Vm) entity).getStatus());
                    break;
                case EVENT:
                    position = getEventStatusPosition(today, (Event) entity);
                    if (position != DashboardPosition.UNKNOWN) {
                        entityCount++;
                    }
                    break;
                default:
                    continue;
            }

            DashboardEntityStatus status = getStatusOnPosition(position);
            if (status != null) {
                status.incrementCount();
            }
        }
    }

    public DashboardEntityStatus getStatusOnPosition(DashboardPosition position) {
        return positionsStatusMap.get(position);
    }

    private DashboardPosition getEventStatusPosition(Date today, Event event) {
        EventSeverity severity = event.getSeverity();

        if (severity == EventSeverity.NORMAL || (severity != EventSeverity.ERROR && event.getTime().before(today))) {
            return DashboardPosition.UNKNOWN;
        }

        switch (event.getSeverity()) {
            case WARNING:
                return DashboardPosition.SECOND;
            case ALERT:
                return DashboardPosition.FIRST;
            case ERROR:
                return DashboardPosition.THIRD;
            default:
                return DashboardPosition.UNKNOWN;
        }
    }

    private Date getToday() {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date today = new Date();
        try {
            today = formatter.parse(formatter.format(today));
        } catch (ParseException e) {
        }

        return today;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DashboardBoxData)) return false;

        DashboardBoxData that = (DashboardBoxData) o;

        return entityClass == that.entityClass;
    }

    @Override
    public int hashCode() {
        return entityClass != null ? entityClass.hashCode() : 0;
    }
}
