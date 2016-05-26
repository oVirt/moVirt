package org.ovirt.mobile.movirt.ui.dashboard;

/**
 * Created by suomiy on 5/16/16.
 */
public class DashboardEntityStatus {
    private int count = 0;
    private Integer iconResourceId = 0;
    private Integer positiveIconResourceId = null;

    public void incrementCount() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getIconResourceId() {
        return count == 0 || positiveIconResourceId == null ? iconResourceId : positiveIconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public void setIconResourceId(int iconResourceId, int positiveCountIconResourceId) {
        setIconResourceId(iconResourceId);
        positiveIconResourceId = positiveCountIconResourceId;
    }
}
