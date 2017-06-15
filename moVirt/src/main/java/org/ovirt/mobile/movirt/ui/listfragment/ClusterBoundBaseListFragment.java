package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtAccountEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import static org.ovirt.mobile.movirt.provider.OVirtContract.HasCluster.CLUSTER_ID;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class ClusterBoundBaseListFragment<E extends OVirtAccountEntity & OVirtContract.HasCluster>
        extends MultipleFacadeBaseListFragment<E> {

    public ClusterBoundBaseListFragment(Class<E> clazz) {
        super(clazz);
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);

        if (isMultiple() && activeSelection.isCluster()) {
            query.where(CLUSTER_ID, activeSelection.getClusterId());
        }
    }
}


