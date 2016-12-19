package org.ovirt.mobile.movirt.ui;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;

import static org.ovirt.mobile.movirt.provider.OVirtContract.HasCluster.CLUSTER_ID;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class ClusterBoundBaseEntityListFragment<E extends OVirtEntity & OVirtContract.HasCluster>
        extends BaseEntityListFragment<E> implements SelectedClusterAware {

    @InstanceState
    protected String clusterId;

    public ClusterBoundBaseEntityListFragment(Class<E> clazz) {
        super(clazz);
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);

        if (clusterId != null) {
            query.where(CLUSTER_ID, clusterId);
        }
    }

    @Override
    public void updateSelectedClusterId(String selectedClusterId) {
        resetListViewPosition();
        this.clusterId = selectedClusterId;
        restartLoader();
    }
}


