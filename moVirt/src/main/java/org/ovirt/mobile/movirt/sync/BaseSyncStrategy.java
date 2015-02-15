package org.ovirt.mobile.movirt.sync;

import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.ovirt.mobile.movirt.Broadcasts;

import java.util.ArrayList;
import java.util.List;

@EBean
public abstract class BaseSyncStrategy implements SyncStrategy {

    @RootContext
    protected Context context;

    protected static interface WrapPredicate<E> {
        boolean toWrap(E entity);
    }

    protected static <E, R extends RestEntityWrapper<E>> List<E> mapRestWrappers(List<R> wrappers, WrapPredicate<R> predicate) {
        List<E> entities = new ArrayList<>();
        if (wrappers == null) {
            return entities;
        }
        for (R rest : wrappers) {
            if (predicate == null || predicate.toWrap(rest)) {
                entities.add(rest.toEntity());
            }
        }
        return entities;
    }

    protected void fireConnectionError(String msg) {
        Intent intent = new Intent(Broadcasts.CONNECTION_FAILURE);
        intent.putExtra(Broadcasts.Extras.CONNECTION_FAILURE_REASON, msg);
        context.sendBroadcast(intent);
    }
}
