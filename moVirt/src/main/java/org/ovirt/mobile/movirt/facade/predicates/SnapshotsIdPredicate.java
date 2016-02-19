package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.model.Snapshot;
import org.ovirt.mobile.movirt.model.SnapshotEmbeddableEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by suomiy on 2/3/16.
 */
public class SnapshotsIdPredicate<T extends SnapshotEmbeddableEntity> implements Predicate<T> {
    private Set<String> ids = new HashSet<>();

    public SnapshotsIdPredicate(List<Snapshot> snapshots) {
        for (Snapshot snapshot : snapshots) {
            ids.add(snapshot.getId());
        }
    }

    @Override
    public boolean apply(T t) {
        return t.isSnapshotEmbedded() && ids.contains(t.getSnapshotId());
    }
}