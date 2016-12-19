package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import static org.ovirt.mobile.movirt.provider.OVirtContract.HasVm.VM_ID;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class VmBoundResumeSyncableBaseEntityListFragment<E extends OVirtEntity & OVirtContract.HasVmAbstract> extends ResumeSyncableBaseEntityListFragment<E> {

    @InstanceState
    protected String vmId;

    public VmBoundResumeSyncableBaseEntityListFragment(Class<E> clazz) {
        super(clazz);
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getVmId() {
        return vmId;
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);

        if (vmId != null) {
            query.where(getVmColumn(), vmId);
        }
    }

    protected String getVmColumn() {
        return VM_ID; // default
    }
}


