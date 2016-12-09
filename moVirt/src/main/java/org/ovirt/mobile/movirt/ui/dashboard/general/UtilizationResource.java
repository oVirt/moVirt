package org.ovirt.mobile.movirt.ui.dashboard.general;

import org.ovirt.mobile.movirt.util.UsageResource;

class UtilizationResource {
    private UsageResource available;
    private UsageResource used;
    private UsageResource total;

    public UtilizationResource(UsageResource used, UsageResource total, UsageResource available) {
        this.used = used;
        this.total = total;
        this.available = available;
    }

    public UsageResource getUsed() {
        return used;
    }

    public void setUsed(UsageResource used) {
        this.used = used;
    }

    public UsageResource getTotal() {
        return total;
    }

    public void setTotal(UsageResource total) {
        this.total = total;
    }

    public UsageResource getAvailable() {
        return available;
    }

    public void setAvailable(UsageResource available) {
        this.available = available;
    }
}
