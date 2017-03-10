package org.ovirt.mobile.movirt.util;

import java.util.Collection;

import io.reactivex.disposables.Disposable;

public class RxHelper {

    public static void dispose(Collection<Disposable> disposables) {
        if (disposables == null) {
            return;
        }

        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        disposables.clear();
    }
}
