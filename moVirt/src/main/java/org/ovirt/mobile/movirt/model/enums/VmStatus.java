package org.ovirt.mobile.movirt.model.enums;

import org.ovirt.mobile.movirt.R;

public enum VmStatus {
    UNASSIGNED(R.drawable.vm_question_mark),
    DOWN(R.drawable.down),
    UP(R.drawable.up),
    POWERING_UP(R.drawable.vm_powering_up),
    PAUSED(R.drawable.vm_paused),
    MIGRATING(R.drawable.vm_migrating),
    UNKNOWN(R.drawable.vm_question_mark),
    NOT_RESPONDING(R.drawable.vm_question_mark),
    WAIT_FOR_LAUNCH(R.drawable.vm_wait_for_launch),
    REBOOT_IN_PROGRESS(R.drawable.vm_reboot_in_progress),
    SAVING_STATE(R.drawable.vm_wait),
    SUSPENDED(R.drawable.vm_suspened),
    IMAGE_LOCKED(R.drawable.vm_wait),
    POWERING_DOWN(R.drawable.vm_powering_down),
    RESTORING_STATE(R.drawable.vm_powering_up);

    VmStatus(int resource) {
        this.resource = resource;
    }

    private final int resource;

    public int getResource() {
        return resource;
    }

    public static VmStatus fromString(String status) {
        try {
            return VmStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return VmStatus.UNKNOWN;
        }
    }
}
