package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Console;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class ConsoleFacade extends BaseEntityFacade<Console> {

    public ConsoleFacade() {
        super(Console.class);
    }

    @Override
    public Intent getDetailIntent(Console entity, Context context) {
        return null;
    }

    @Override
    protected OVirtClient.Request<Console> getSyncOneRestRequest(String consoleId, String... ids) {
        throw new UnsupportedOperationException("Standalone console is a vv file!");
    }

    @Override
    protected OVirtClient.Request<List<Console>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getConsolesRequest(vmId);
    }
}

