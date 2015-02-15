package org.ovirt.mobile.movirt.sync;

import android.os.RemoteException;

public class SimpleResponse<T> implements Response<T> {

    @Override
    public void before() {
        // do nothing
    }

    @Override
    public void onResponse(T t) throws RemoteException {
        // do nothing
    }

    @Override
    public void onError() {
        // do nothing
    }

    @Override
    public void after() {
        // do nothing
    }
}
