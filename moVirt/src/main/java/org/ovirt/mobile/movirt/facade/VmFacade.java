package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.properties.manager.AccountPropertiesManager;
import org.ovirt.mobile.movirt.auth.properties.property.version.support.VersionSupport;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.VmTriggerResolver;
import org.ovirt.mobile.movirt.rest.CompositeResponse;
import org.ovirt.mobile.movirt.rest.Request;
import org.ovirt.mobile.movirt.rest.Response;
import org.ovirt.mobile.movirt.rest.SimpleResponse;
import org.ovirt.mobile.movirt.ui.vms.VmDetailActivity_;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class VmFacade extends BaseEntityFacade<Vm> {

    @Bean
    VmTriggerResolver triggerResolver;

    @Bean
    AccountPropertiesManager propertiesManager;

    public VmFacade() {
        super(Vm.class);
    }

    @Override
    public Intent getDetailIntent(Vm entity, Context context) {
        Intent intent = new Intent(context, VmDetailActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(entity.getUri());
        return intent;
    }

    @Override
    protected CompositeResponse<Vm> getSyncOneResponse(final Response<Vm> response, String... ids) {
        requireSignature(ids);
        CompositeResponse<Vm> res = super.getSyncOneResponse(response);
        if (VersionSupport.NICS_POLLED_WITH_VMS.isSupported(propertiesManager.getApiVersion())) {
            res.addResponse(new SimpleResponse<Vm>() {
                @Override
                public void onResponse(Vm entity) throws RemoteException {
                    String vmId = entity.getId();
                    syncAdapter.updateLocalEntities(entity.getNics(), Nic.class, new VmIdPredicate<Nic>(vmId));
                }
            });
        }

        return res;
    }

    @Override
    protected CompositeResponse<List<Vm>> getSyncAllResponse(final Response<List<Vm>> response, String... ids) {
        requireSignature(ids);
        CompositeResponse<List<Vm>> res = new CompositeResponse<>(
                syncAdapter.getUpdateEntitiesResponse(Vm.class), response);

        if (VersionSupport.NICS_POLLED_WITH_VMS.isSupported(propertiesManager.getApiVersion())) {
            res.addResponse(new SimpleResponse<List<Vm>>() {
                @Override
                public void onResponse(List<Vm> entities) throws RemoteException {
                    List<Nic> nics = new ArrayList<>();
                    for (Vm vm : entities) {
                        nics.addAll(vm.getNics());
                        vm.setNics(null);
                    }
                    syncAdapter.updateLocalEntities(nics, Nic.class);
                }
            });
        }

        return res;
    }

    @Override
    protected Request<Vm> getSyncOneRestRequest(String vmId, String... ids) {
        requireSignature(ids);
        return oVirtClient.getVmRequest(vmId);
    }

    @Override
    protected Request<List<Vm>> getSyncAllRestRequest(String... ids) {
        requireSignature(ids);
        return oVirtClient.getVmsRequest();
    }

    @Override
    public Collection<Trigger<Vm>> getAllTriggers() {
        return triggerResolver.getAllTriggers();
    }

    @Override
    public List<Trigger<Vm>> getTriggers(Vm entity, Collection<Trigger<Vm>> allTriggers) {
        return triggerResolver.getTriggers(entity, allTriggers);
    }
}
