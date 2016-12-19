package org.ovirt.mobile.movirt.ui;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract.HasVmAbstract;
import org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotEmbeddableEntity;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import static org.ovirt.mobile.movirt.provider.OVirtContract.SnapshotEmbeddableEntity.SNAPSHOT_ID;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class SnapshotEmbeddableVmBoundResumeSyncableBaseEntityListFragment<E extends OVirtEntity & SnapshotEmbeddableEntity & HasVmAbstract>
        extends VmBoundResumeSyncableBaseEntityListFragment<E> {

    @InstanceState
    protected String snapshotId;

    public SnapshotEmbeddableVmBoundResumeSyncableBaseEntityListFragment(Class<E> clazz) {
        super(clazz);
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);

        if (snapshotId == null) {
            query.empty(SNAPSHOT_ID);
        } else {
            query.where(SNAPSHOT_ID, snapshotId);
        }
    }
}


