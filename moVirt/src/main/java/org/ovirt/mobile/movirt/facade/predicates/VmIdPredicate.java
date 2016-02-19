package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.provider.OVirtContract.HasVm;

/**
 * Created by suomiy on 2/3/16.
 */
public class VmIdPredicate<T extends HasVm> implements Predicate<T> {
    private final String vmId;

    public VmIdPredicate(String vmId) {
        this.vmId = vmId;
    }

    @Override
    public boolean apply(T t) {
        return vmId.equals(t.getVmId());
    }
}
