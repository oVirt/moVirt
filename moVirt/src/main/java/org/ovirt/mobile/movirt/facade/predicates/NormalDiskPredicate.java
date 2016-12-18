package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.model.Disk;

public class NormalDiskPredicate implements Predicate<Disk> {

    // all normal disks should have empty vmId and empty snapshotId
    private static Predicate<Disk> implementation =
            new AndPredicate<>(new EmptyVmIdPredicate<Disk>(), new NotSnapshotEmbeddedPredicate<Disk>());

    @Override
    public boolean apply(Disk disk) {
        return implementation.apply(disk);
    }
}
