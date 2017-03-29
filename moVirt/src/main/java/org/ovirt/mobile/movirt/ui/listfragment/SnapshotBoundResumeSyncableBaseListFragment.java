package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.OVirtContract.HasVmAbstract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import static org.ovirt.mobile.movirt.provider.OVirtContract.HasSnapshot.SNAPSHOT_ID;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class SnapshotBoundResumeSyncableBaseListFragment<E extends OVirtEntity & OVirtContract.HasSnapshot & HasVmAbstract>
        extends VmBoundResumeSyncableBaseListFragment<E> {

    @InstanceState
    protected String snapshotId;

    public SnapshotBoundResumeSyncableBaseListFragment(Class<E> clazz) {
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
        query.where(SNAPSHOT_ID, snapshotId);
    }
}


