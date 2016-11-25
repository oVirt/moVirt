package org.ovirt.mobile.movirt.ui;

import org.ovirt.mobile.movirt.model.OVirtEntity;

public abstract class ResumeSyncableBaseEntityListFragment<E extends OVirtEntity> extends BaseEntityListFragment<E> {

    private boolean synced = false;

    public ResumeSyncableBaseEntityListFragment(Class<E> clazz) {
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

