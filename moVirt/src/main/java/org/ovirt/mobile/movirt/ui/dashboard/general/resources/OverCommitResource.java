package org.ovirt.mobile.movirt.ui.dashboard.general.resources;

import org.ovirt.mobile.movirt.util.usage.UsageResource;

public class OverCommitResource {
    private UsageResource physicalTotal;
    private UsageResource virtualUsed;
    private UsageResource virtualTotal;

    public OverCommitResource() {
    }

    public OverCommitResource(UsageResource physicalTotal, UsageResource virtualUsed, UsageResource virtualTotal) {
        this.physicalTotal = physicalTotal;
        this.virtualUsed = virtualUsed;
        this.virtualTotal = virtualTotal;
    }

    public UsageResource getPhysicalTotal() {
        return physicalTotal;
    }

    public void setPhysicalTotal(UsageResource physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    public UsageResource getVirtualUsed() {
        return virtualUsed;
    }

    public void setVirtualUsed(UsageResource virtualUsed) {
        this.virtualUsed = virtualUsed;
    }

    public UsageResource getVirtualTotal() {
        return virtualTotal;
    }

    public void setVirtualTotal(UsageResource virtualTotal) {
        this.virtualTotal = virtualTotal;
    }

    /**
     * Return the current used virtual allocated resources in relation to the actual resources. The calculation is
     * Running virtual resources / Actual resources * 100. This returns the ratio of running virtual resources
     * in relation to the actual resources.
     *
     * @return A percentage indicating the running virtual resource compared to actual resources.
     */
    public long getOvercommit() {
        if (virtualUsed == null || physicalTotal == null) {
            return 0;
        }

        return (long) (virtualUsed.getValue() / (double) (physicalTotal.getValue() == 0 ? 1 : physicalTotal.getValue()) * 100);
    }

    /**
     * Return the virtual allocated resources in relation to the actual resources. The calculation is
     * Allocated virtual resources / Actual resources * 100. This returns the ratio of allocated virtual resources
     * in relation to the actual resources.
     *
     * @return A percentage indicating the allocated virtual resource compared to actual resources.
     */
    public long getAllocated() {
        if (virtualTotal == null || physicalTotal == null) {
            return 0;
        }
        return (long) (virtualTotal.getValue() / (double) (physicalTotal.getValue() == 0 ? 1 : physicalTotal.getValue()) * 100);
    }

    public boolean isInitialized() {
        return virtualUsed != null && virtualTotal != null && physicalTotal != null;
    }
}
