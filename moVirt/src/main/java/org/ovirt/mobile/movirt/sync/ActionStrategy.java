package org.ovirt.mobile.movirt.sync;

import org.ovirt.mobile.movirt.sync.rest.ActionTicket;

public interface ActionStrategy {
    void startVm(String vmId);

    void stopVm(String vmId);

    void rebootVm(String vmId);

    void getConsoleTicket(String vmId, Response<ActionTicket> response);

    String login(String apiUrl, String username, String password, boolean disableHttps, boolean hasAdminPrivileges);
}
