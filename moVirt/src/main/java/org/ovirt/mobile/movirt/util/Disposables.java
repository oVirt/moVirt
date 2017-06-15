package org.ovirt.mobile.movirt.util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class Disposables {

    private final List<Disposable> disposables = new ArrayList<>(5);

    public Disposables add(Disposable disposable) {
        disposables.add(disposable);
        return this;
    }

    public void destroy() {
        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        disposables.clear();
    }

    public void destroyLastDisposable() {
        Disposable disposable = disposables.remove(disposables.size() - 1);

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
