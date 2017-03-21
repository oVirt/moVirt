package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class ResumeSyncableBaseListFragment<E extends OVirtEntity> extends FacadeBaseListFragment<E> {

    @InstanceState
    protected boolean synced = false;

    public ResumeSyncableBaseListFragment(Class<E> clazz) {
        super(clazz);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!synced && isResumeSyncable()) {
            synced = true;
            onRefresh();
        }
    }

    // override for customizable behaviour of fragment
    public boolean isResumeSyncable() {
        return true;
    }
}

