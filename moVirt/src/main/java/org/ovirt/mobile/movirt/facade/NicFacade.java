package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.rest.OVirtClient;

import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class NicFacade extends BaseEntityFacade<Nic> {

    public NicFacade() {
        super(Nic.class);
    }

    @Override
    public Intent getDetailIntent(Nic entity, Context context) {
        return null;
    }

    @Override
    protected OVirtClient.Request<Nic> getSyncOneRestRequest(String nicId, String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getNicRequest(vmId, nicId);
    }

    @Override
    protected OVirtClient.Request<List<Nic>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return oVirtClient.getNicsRequest(vmId);
    }

    @Override
    protected OVirtClient.CompositeResponse<List<Nic>> getSyncAllResponse(final OVirtClient.Response<List<Nic>> response, final String... ids) {
        requireSignature(ids, "vmId");
        String vmId = ids[0];
        return new OVirtClient.CompositeResponse<>(syncAdapter.getUpdateEntitiesResponse(Nic.class, new VmIdPredicate<Nic>(vmId)), response);
    }
}
