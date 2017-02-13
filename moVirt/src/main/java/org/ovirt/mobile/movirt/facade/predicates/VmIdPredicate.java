package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.provider.OVirtContract.HasVm;

public class VmIdPredicate<T extends HasVm> implements Predicate<T> {
    private final String vmId;

    public VmIdPredicate(String vmId) {
        if (vmId == null) {
            throw new IllegalArgumentException("vmId is null");
        }
        this.vmId = vmId;
    }

    @Override
    public boolean apply(T t) {
        return vmId.equals(t.getVmId());
    }
}
