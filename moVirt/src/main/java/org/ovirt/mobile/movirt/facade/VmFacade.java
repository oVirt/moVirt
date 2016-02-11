package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.facade.predicates.NotSnapshotEmbeddedPredicate;
import org.ovirt.mobile.movirt.facade.predicates.VmIdPredicate;
import org.ovirt.mobile.movirt.model.Disk;
import org.ovirt.mobile.movirt.model.Nic;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.VmTriggerResolver;
import org.ovirt.mobile.movirt.rest.OVirtClient;
import org.ovirt.mobile.movirt.ui.vms.VmDetailActivity_;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.ovirt.mobile.movirt.util.ObjectUtils.requireSignature;

@EBean
public class VmFacade extends BaseEntityFacade<Vm> {

    @Bean
    VmTriggerResolver triggerResolver;

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
    protected OVirtClient.CompositeResponse<Vm> getSyncOneResponse(final OVirtClient.Response<Vm> response, String... ids) {
        requireSignature(ids);
        OVirtClient.CompositeResponse<Vm> res = super.getSyncOneResponse(response);
        res.addResponse(new OVirtClient.SimpleResponse<Vm>() {
            @Override
            public void onResponse(Vm entity) throws RemoteException {
                String vmId = entity.getId();
                syncAdapter.updateLocalEntities(entity.getDisks(), Disk.class, new VmIdPredicate<Disk>(vmId));
                syncAdapter.updateLocalEntities(entity.getNics(), Nic.class, new VmIdPredicate<Nic>(vmId));
            }
        });

        return res;
    }

    @Override
    protected OVirtClient.CompositeResponse<List<Vm>> getSyncAllResponse(final OVirtClient.Response<List<Vm>> response, String... ids) {
        requireSignature(ids);
        OVirtClient.CompositeResponse<List<Vm>> res = new OVirtClient.CompositeResponse<>(
                syncAdapter.getUpdateEntitiesResponse(Vm.class, new NotSnapshotEmbeddedPredicate<Vm>()),
                response);

        res.addResponse(new OVirtClient.SimpleResponse<List<Vm>>() {
            @Override
            public void onResponse(List<Vm> entities) throws RemoteException {
                List<Disk> disks = new ArrayList<>();
                List<Nic> nics = new ArrayList<>();
                for (Vm vm : entities) {
                    disks.addAll(vm.getDisks());
                    nics.addAll(vm.getNics());
                }

                syncAdapter.updateLocalEntities(disks, Disk.class, new NotSnapshotEmbeddedPredicate<Disk>());
                syncAdapter.updateLocalEntities(nics, Nic.class);
            }
        });

        return res;
    }

    @Override
    protected OVirtClient.Request<Vm> getSyncOneRestRequest(String vmId, String... ids) {
        requireSignature(ids);
        return oVirtClient.getVmRequest(vmId);
    }

    @Override
    protected OVirtClient.Request<List<Vm>> getSyncAllRestRequest(String... ids) {
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
