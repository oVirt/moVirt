package org.ovirt.mobile.movirt.model.enums;

import java.util.Arrays;
import java.util.List;

public enum HostCommand {
    ACTIVATE(HostStatus.MAINTENANCE, HostStatus.ERROR, HostStatus.PREPARING_FOR_MAINTENANCE,
            HostStatus.NON_OPERATIONAL, HostStatus.INSTALL_FAILED),
    DEACTIVATE(HostStatus.UP, HostStatus.ERROR, HostStatus.NON_RESPONSIVE, HostStatus.NON_OPERATIONAL,
            HostStatus.INSTALL_FAILED, HostStatus.DOWN);

    private final List<HostStatus> validStates;

    public List<HostStatus> getValidStates() {
        return validStates;
    }

    HostCommand(HostStatus... validStates) {
        this.validStates = Arrays.asList(validStates);
    }

    public boolean canExecute(HostStatus status) {
        return validStates.contains(status);
    }
}
