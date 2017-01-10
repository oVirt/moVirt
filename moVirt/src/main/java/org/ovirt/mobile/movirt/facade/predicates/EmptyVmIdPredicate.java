package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.provider.OVirtContract.HasVm;
import org.springframework.util.StringUtils;

public class EmptyVmIdPredicate<T extends HasVm> implements Predicate<T> {

    @Override
    public boolean apply(T t) {
        return StringUtils.isEmpty(t.getVmId());
    }
}
