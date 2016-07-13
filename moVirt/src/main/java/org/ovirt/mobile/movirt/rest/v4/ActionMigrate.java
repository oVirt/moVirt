package org.ovirt.mobile.movirt.rest.v4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.ovirt.mobile.movirt.rest.Action;

/**
 * Action containing Host to migrate to
 * Created by Nika on 31.07.2015.
 */
@JsonRootName("action")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionMigrate extends Action {
    public Host host;

    public ActionMigrate(String hostId) {
        host = new Host();
        host.id = hostId;
    }
}
