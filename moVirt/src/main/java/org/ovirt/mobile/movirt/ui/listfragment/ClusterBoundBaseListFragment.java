package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.SelectedClusterAware;

import static org.ovirt.mobile.movirt.provider.OVirtContract.HasCluster.CLUSTER_ID;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class ClusterBoundBaseListFragment<E extends OVirtEntity & OVirtContract.HasCluster>
        extends FacadeBaseListFragment<E> implements SelectedClusterAware {

    @InstanceState
    protected String clusterId;

    public ClusterBoundBaseListFragment(Class<E> clazz) {
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


