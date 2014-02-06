package org.ovirt.mobile.movirt.provider;

import com.blandware.android.atleap.provider.ormlite.OrmLiteUriMatcher;

import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Vm;

public class OVirtUriMatcher extends OrmLiteUriMatcher {
    public OVirtUriMatcher(String authority) {
        super(authority);
    }

    @Override
    public void instantiate() {
        addClass(OVirtContract.PATH_VMS, Vm.class);
        addClass(OVirtContract.PATH_VM, Vm.class);

        addClass(OVirtContract.PATH_CLUSTERS, Cluster.class);
        addClass(OVirtContract.PATH_CLUSTER, Cluster.class);
    }
}
