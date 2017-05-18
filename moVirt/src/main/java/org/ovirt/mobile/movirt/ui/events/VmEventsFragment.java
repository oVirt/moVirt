package org.ovirt.mobile.movirt.ui.events;

import android.text.TextUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.auth.account.AccountDeletedException;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.rest.Response;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.VM_ID;

@EFragment(R.layout.fragment_base_entity_list)
public class VmEventsFragment extends NamedEntityResumeSyncableEventsFragment {

    @InstanceState
    String vmId;

    @AfterViews
    protected void init2() {
        if (vmId == null) {
            throw new IllegalArgumentException("vmId is null");
        }
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<Event> query) {
        super.appendQuery(query);
        query.where(VM_ID, vmId);
    }

    public VmEventsFragment setVmId(String vmId) {
        this.vmId = vmId;
        return this;
    }

    @Override
    protected void sync(Response<List<Event>> response) {
        String vmName = getEntityName(Vm.class, vmId);
        if (!TextUtils.isEmpty(vmName)) {
            try {
                environmentStore.getEnvironment(account).getVmEventFacade().syncAll(response, vmId, vmName);
            } catch (AccountDeletedException ignore) {
            }
        }
    }
}
