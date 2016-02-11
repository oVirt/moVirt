package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.model.SnapshotEmbeddableEntity;

/**
 * Created by suomiy on 2/3/16.
 */
public class SnapshotIdPredicate<T extends SnapshotEmbeddableEntity> implements Predicate<T> {
    private String snapshotId;

    public SnapshotIdPredicate(String snapshotsId) {
        this.snapshotId = snapshotsId;
    }

    @Override
    public boolean apply(T t) {
        return t.isSnapshotEmbedded() && snapshotId.equals(t.getSnapshotId());
    }
}
