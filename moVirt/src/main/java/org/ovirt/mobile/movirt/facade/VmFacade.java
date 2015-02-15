package org.ovirt.mobile.movirt.facade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.model.EntityMapper;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.model.trigger.VmTriggerResolver;
import org.ovirt.mobile.movirt.sync.Response;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.ui.vms.VmDetailActivity_;

import java.util.List;

@EBean
public class VmFacade implements EntityFacade<Vm> {

    @Bean
    VmTriggerResolver triggerResolver;

    @Bean
    SyncAdapter syncAdapter;

    @Override
    public Vm mapFromCursor(Cursor cursor) {
        return EntityMapper.forEntity(Vm.class).fromCursor(cursor);
    }

    @Override
    public List<Trigger<Vm>> getTriggers(Vm entity) {
        return triggerResolver.getTriggers(entity);
    }

    @Override
    public Intent getDetailIntent(Vm entity, Context context) {
        Intent intent = new Intent(context, VmDetailActivity_.class);
        intent.setData(entity.getUri());
        return intent;
    }

    @Override
    public void sync(String id, Response<Vm> response) {
        syncAdapter.syncVm(id, response);
    }
}
