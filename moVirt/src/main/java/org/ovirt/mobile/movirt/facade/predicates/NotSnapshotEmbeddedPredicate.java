package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import org.ovirt.mobile.movirt.model.SnapshotEmbeddableEntity;

/**
 * Created by suomiy on 2/3/16.
 */
public class NotSnapshotEmbeddedPredicate<T extends SnapshotEmbeddableEntity> implements Predicate<T> {

    @Override
    public boolean apply(T t) {
        return !t.isSnapshotEmbedded();
    }
}
