package org.ovirt.mobile.movirt.model.enums;

import java.util.Arrays;
import java.util.List;

public enum VmCommand {
    RUN(VmStatus.DOWN, VmStatus.PAUSED),
    STOP(VmStatus.WAIT_FOR_LAUNCH, VmStatus.UP, VmStatus.POWERING_DOWN, VmStatus.POWERING_UP,
            VmStatus.REBOOT_IN_PROGRESS, VmStatus.MIGRATING, VmStatus.SUSPENDED, VmStatus.PAUSED,
            VmStatus.NOT_RESPONDING),
    SHUTDOWN(VmStatus.WAIT_FOR_LAUNCH, VmStatus.UP, VmStatus.POWERING_DOWN, VmStatus.POWERING_UP,
            VmStatus.REBOOT_IN_PROGRESS, VmStatus.MIGRATING, VmStatus.SUSPENDED, VmStatus.PAUSED,
            VmStatus.NOT_RESPONDING),
    REBOOT(VmStatus.UP, VmStatus.POWERING_UP),
    START_MIGRATION(VmStatus.UNASSIGNED, VmStatus.UP, VmStatus.POWERING_UP,
            VmStatus.UNKNOWN, VmStatus.WAIT_FOR_LAUNCH, VmStatus.REBOOT_IN_PROGRESS,
            VmStatus.SAVING_STATE, VmStatus.SUSPENDED, VmStatus.IMAGE_LOCKED, VmStatus.POWERING_DOWN),
    CANCEL_MIGRATION(VmStatus.MIGRATING),
    CONSOLE(VmStatus.UP, VmStatus.POWERING_UP, VmStatus.REBOOT_IN_PROGRESS, VmStatus.POWERING_DOWN,
            VmStatus.PAUSED),
    // used in snapshots
    SAVE_MEMORY(VmStatus.UNASSIGNED, VmStatus.UP, VmStatus.POWERING_UP, VmStatus.PAUSED,
            VmStatus.MIGRATING, VmStatus.UNKNOWN, VmStatus.NOT_RESPONDING, VmStatus.REBOOT_IN_PROGRESS,
            VmStatus.SAVING_STATE, VmStatus.SUSPENDED, VmStatus.IMAGE_LOCKED, VmStatus.POWERING_DOWN),
    NOT_RUNNING(VmStatus.DOWN);

    private final List<VmStatus> validStates;

    public List<VmStatus> getValidStates() {
        return validStates;
    }

    VmCommand(VmStatus... validStates) {
        this.validStates = Arrays.asList(validStates);
    }

    public boolean canExecute(VmStatus status) {
        return validStates.contains(status);
    }
}
